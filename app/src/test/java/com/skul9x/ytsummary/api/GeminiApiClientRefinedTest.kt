package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import okio.buffer

class GeminiApiClientRefinedTest {

    private val apiKeyManager = mockk<ApiKeyManager>()
    private val quotaManager = mockk<ModelQuotaManager>()
    private val modelManager = mockk<ModelManager>()
    private val client = mockk<OkHttpClient>()
    
    private lateinit var apiClient: GeminiApiClient

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        apiClient = GeminiApiClient(
            apiKeyManager = apiKeyManager,
            quotaManager = quotaManager,
            modelManager = modelManager,
            client = client,
            baseUrl = "http://localhost"
        )
        
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("model-A", "model-B")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
    }

    @Test
    fun `429 with Rate Limit body should trigger cooldown instead of exhaustion`() = runTest {
        val errorBody = "Rate limit reached for model-A per minute"
        val response429 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .body(errorBody.toResponseBody(null))
            .build()
            
        val call1 = mockk<Call>()
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response429
        
        coEvery { quotaManager.markCooldown("model-A", "key1") } returns Unit
        
        // Key 2 succeeds
        val call2 = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Success\"}]}}]}".toResponseBody(null))
            .build()
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        apiClient.summarize("test").toList()

        io.mockk.coVerify { quotaManager.markCooldown("model-A", "key1") }
        io.mockk.coVerify(exactly = 0) { quotaManager.markExhausted(any(), any()) }
    }

    @Test
    fun `429 with Quota Exhausted body should trigger exhaustion instead of cooldown`() = runTest {
        val errorBody = "Your daily quota for this model has been exhausted"
        val response429 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .body(errorBody.toResponseBody(null))
            .build()
            
        val call1 = mockk<Call>()
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response429
        
        coEvery { quotaManager.markExhausted("model-A", "key1") } returns Unit
        
        // Key 2 succeeds
        val call2 = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Success\"}]}}]}".toResponseBody(null))
            .build()
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        apiClient.summarize("test").toList()

        io.mockk.coVerify { quotaManager.markExhausted("model-A", "key1") }
        io.mockk.coVerify(exactly = 0) { quotaManager.markCooldown("model-A", "key1") }
    }

    @Test
    fun `400 error should break key loop and move to next model`() = runTest {
        // model-A with key1 returns 400
        val response400 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("Bad Request")
            .body("Context window exceeded".toResponseBody(null))
            .build()
            
        val call1A = mockk<Call>()
        every { client.newCall(match { it.url.toString().contains("model-A") && it.header("x-goog-api-key") == "key1" }) } returns call1A
        every { call1A.execute() } returns response400
        
        // model-B with key1 succeeds
        val call1B = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Success from model-B\"}]}}]}".toResponseBody(null))
            .build()
        every { client.newCall(match { it.url.toString().contains("model-B") && it.header("x-goog-api-key") == "key1" }) } returns call1B
        every { call1B.execute() } returns response200

        val results = apiClient.summarize("test").toList()

        // Should NOT have tried key2 for model-A
        verify(exactly = 0) { client.newCall(match { it.url.toString().contains("model-A") && it.header("x-goog-api-key") == "key2" }) }
        // Should have moved to model-B
        assertTrue(results.any { it is AiResult.Success && it.model == "model-B" })
    }

    @Test
    fun `IOException after hasStarted should emit Error and stop instead of rotating`() = runTest {
        val sseContent = "data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Part 1\"}]}}]}\n"
        val response = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(object : okhttp3.ResponseBody() {
                override fun contentType() = null
                override fun contentLength() = -1L
                override fun source(): okio.BufferedSource {
                    val buffer = okio.Buffer()
                    buffer.writeUtf8(sseContent)
                    val forwardingSource = object : okio.ForwardingSource(buffer) {
                        override fun read(sink: okio.Buffer, byteCount: Long): Long {
                            val read = super.read(sink, byteCount)
                            if (read == -1L) throw IOException("Network interrupted")
                            return read
                        }
                    }
                    return forwardingSource.buffer()
                }
            })
            .build()

        val call = mockk<Call>()
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response

        val results = apiClient.summarize("test").toList()

        // Expected: DeltaSuccess("Part 1"), AiResult.Error(...)
        assertTrue(results.any { it is AiResult.DeltaSuccess })
        assertTrue(results.any { it is AiResult.Error })
        // Should NOT have rotated to key2
        verify(exactly = 1) { client.newCall(any()) }
    }

}
