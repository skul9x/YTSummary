package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.net.ConnectException

class GeminiApiClientNetworkTerminationTest {

    private val apiKeyManager = mockk<ApiKeyManager>()
    private val quotaManager = mockk<ModelQuotaManager>()
    private val modelManager = mockk<ModelManager>()
    private val client = mockk<OkHttpClient>()
    
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
            baseUrl = "http://localhost"
        )
    }

    @Test
    fun testUnknownHostExceptionTerminatesRotationImmediately() = runTest {
        // Arrange: 2 keys, key1 throws UnknownHostException, key2 should NOT be called
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("models/gemini-3.1-flash-lite")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true

        val call1 = mockk<okhttp3.Call>()
        every { client.newCall(match<Request> { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } throws UnknownHostException("No address associated with hostname")

        // Act
        val results = apiClient.summarize("test").toList()

        // Assert
        assertEquals(1, results.size)
        assertTrue(results[0] is AiResult.Error)
        val errorMessage = (results[0] as AiResult.Error).message
        assertTrue(errorMessage.contains("mạng", ignoreCase = true))

        // Verify key2 was never called
        verify(exactly = 0) { client.newCall(match<Request> { it.header("x-goog-api-key") == "key2" }) }
    }

    @Test
    fun testConnectExceptionTerminatesRotationImmediately() = runTest {
        // Arrange: 2 keys, key1 throws ConnectException, key2 should NOT be called
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("models/gemini-3.1-flash-lite")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true

        val call1 = mockk<okhttp3.Call>()
        every { client.newCall(match<Request> { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } throws ConnectException("Failed to connect to localhost")

        // Act
        val results = apiClient.summarize("test").toList()

        // Assert
        assertEquals(1, results.size)
        assertTrue(results[0] is AiResult.Error)
        val errorMessage = (results[0] as AiResult.Error).message
        assertTrue(errorMessage.contains("mạng", ignoreCase = true))

        // Verify key2 was never called
        verify(exactly = 0) { client.newCall(match<Request> { it.header("x-goog-api-key") == "key2" }) }
    }
}
