package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeminiApiClientCooldownDelayTest {

    private val apiKeyManager = mockk<ApiKeyManager>()
    private val quotaManager = mockk<ModelQuotaManager>()
    private val modelManager = mockk<ModelManager>()
    private val client = mockk<OkHttpClient>()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var apiClient: GeminiApiClient

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any<String>(), any<String>()) } returns 0
        every { android.util.Log.e(any<String>(), any<String>()) } returns 0
        every { android.util.Log.w(any<String>(), any<String>()) } returns 0

        apiClient = GeminiApiClient(
            apiKeyManager = apiKeyManager,
            quotaManager = quotaManager,
            modelManager = modelManager,
            client = client,
            baseUrl = "http://localhost",
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun testServerBusyDelayIsApplied() = runTest(testDispatcher) {
        // Arrange: 2 keys, key1 returns 503, key2 returns 200
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("models/gemini-3.1-flash-lite")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
        coEvery { quotaManager.markCooldown(any(), "key1") } returns Unit

        val call1 = mockk<okhttp3.Call>()
        val response503 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(503)
            .message("Service Unavailable")
            .body("Server busy".toResponseBody(null))
            .build()
        
        val call2 = mockk<okhttp3.Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Done!\"}]}}]}".toResponseBody(null))
            .build()
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response503
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        val startTime = currentTime
        
        // Act
        val results = apiClient.summarize("test").toList()

        val endTime = currentTime
        val elapsedTime = endTime - startTime

        // Assert
        assertTrue(results.any { it is AiResult.Success && it.text == "Done!" })
        // Check that delay of 300ms was applied between key1 and key2
        assertTrue("Expected delay of at least 300ms, but was $elapsedTime ms", elapsedTime >= 300L)
    }
}
