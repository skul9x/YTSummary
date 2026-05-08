package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.*
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

class GeminiApiClientDiagnosticTest {

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
    }

    @Test
    fun `BUG 1 - 429 should call markExhausted`() = runTest {
        every { apiKeyManager.getApiKeys() } returns listOf("key1")
        every { modelManager.getModels() } returns listOf("gemini-1.5-flash")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
        coEvery { quotaManager.markExhausted(any(), any()) } returns Unit

        val call = mockk<Call>()
        val response429 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .body("".toResponseBody(null))
            .build()
        
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response429

        apiClient.summarize("test").toList()

        coVerify { quotaManager.markExhausted(any(), "key1") }
    }

    @Test
    fun `BUG 3 FIX - 400 error should rotate models NOT keys`() = runTest {
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("model1", "model2")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
        coEvery { quotaManager.markExhausted(any(), any()) } returns Unit

        val call1A = mockk<Call>()
        val response400 = Response.Builder()
            .request(Request.Builder().url("http://localhost/model1").build())
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("Bad Request")
            .body("".toResponseBody(null))
            .build()

        val call1B = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost/model2").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Success from model2\"}]}}]}".toResponseBody(null))
            .build()

        every { client.newCall(match { it.url.toString().contains("model1") && it.header("x-goog-api-key") == "key1" }) } returns call1A
        every { call1A.execute() } returns response400

        every { client.newCall(match { it.url.toString().contains("model2") && it.header("x-goog-api-key") == "key1" }) } returns call1B
        every { call1B.execute() } returns response200

        val results = apiClient.summarize("test").toList()

        assertTrue(results.any { it is AiResult.Success && it.model == "model2" })
        
        // Should have tried model1 key1
        verify(exactly = 1) { client.newCall(match { it.url.toString().contains("model1") && it.header("x-goog-api-key") == "key1" }) }
        // Should NOT have tried model1 key2
        verify(exactly = 0) { client.newCall(match { it.url.toString().contains("model1") && it.header("x-goog-api-key") == "key2" }) }
        // Should have tried model2 key1
        verify(exactly = 1) { client.newCall(match { it.url.toString().contains("model2") && it.header("x-goog-api-key") == "key1" }) }
    }
}

