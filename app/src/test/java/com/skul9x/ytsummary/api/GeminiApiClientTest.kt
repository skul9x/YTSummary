package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiApiClientTest {

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
        
        every { apiKeyManager.getApiKeys() } returns listOf("key1")
        every { modelManager.getModels() } returns listOf("gemini-1.5-flash")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
    }

    @Test
    fun `summarize should emit DeltaSuccess and final Success`() = runTest {
        // Arrange
        val sseContent = "data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Hello \"}]}}]}\r\ndata: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"world!\"}]}}]}\r\n"
        
        val responseBody = sseContent.toResponseBody("text/event-stream".toMediaTypeOrNull())
        val response = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()

        val call = mockk<Call>()
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response

        // Act
        val results = apiClient.summarize("test transcript").toList()

        // Assert
        // Expected: DeltaSuccess("Hello "), DeltaSuccess("world!"), Success("Hello world!")
        assertEquals(3, results.size)
        assertTrue(results[0] is AiResult.DeltaSuccess)
        assertEquals("Hello ", (results[0] as AiResult.DeltaSuccess).chunk)
        
        assertTrue(results[1] is AiResult.DeltaSuccess)
        assertEquals("world!", (results[1] as AiResult.DeltaSuccess).chunk)
        
        assertTrue(results[2] is AiResult.Success)
        assertEquals("Hello world!", (results[2] as AiResult.Success).text)
    }

    @Test
    fun `summarize should rotate keys on 429 error`() = runTest {
        // Arrange: 2 keys, key1 returns 429, key2 returns 200
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        coEvery { quotaManager.markExhausted(any(), "key1") } returns Unit
        
        val call1 = mockk<Call>()
        val response429 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .body("".toResponseBody(null))
            .build()
        
        val call2 = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"From key2\"}]}}]}".toResponseBody(null))
            .build()
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response429
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        // Act
        val results = apiClient.summarize("test").toList()

        // Assert
        assertTrue(results.any { it is AiResult.Success && it.text == "From key2" })
    }

    @Test
    fun `summarize should rotate keys on 503 error`() = runTest {
        // Arrange: 2 keys, key1 returns 503, key2 returns 200
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        coEvery { quotaManager.markCooldown(any(), "key1") } returns Unit
        
        val call1 = mockk<Call>()
        val response503 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(503)
            .message("Service Unavailable")
            .body("".toResponseBody(null))
            .build()
        
        val call2 = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"From key2\"}]}}]}".toResponseBody(null))
            .build()
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response503
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        // Act
        val results = apiClient.summarize("test").toList()

        // Assert
        assertTrue(results.any { it is AiResult.Success && it.text == "From key2" })
    }
}
