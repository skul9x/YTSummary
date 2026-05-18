package com.skul9x.ytsummary.transcript

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Integration Test to verify real transcript fetching.
 * Note: Skip in CI/Cloud environment to prevent build flakiness due to YouTube IP block/network restriction.
 */
class YouTubeTranscriptIntegrationTest {

    private lateinit var service: YouTubeTranscriptService
    private val videoId = "MbOah3Fjkhc"

    @Before
    fun setup() {
        // Skip integration test when running on CI server to avoid flakiness from YouTube IP blocking/rate-limits
        val isCI = System.getenv("CI") == "true" || System.getenv("GITHUB_ACTIONS") == "true"
        assumeFalse("Skipping integration test in CI environment due to YouTube network/IP restrictions.", isCI)

        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        service = YouTubeTranscriptService(client)
    }

    @Test
    fun `test fetch transcript from real video`() = runBlocking {
        println("🚀 Fetching transcript for: $videoId...")
        
        val result = service.fetchTranscript(videoId, listOf("vi", "en"))
        
        if (result.isSuccess) {
            val text = result.getOrNull() ?: ""
            println("✅ SUCCESS!")
            println("📝 Length: ${text.length} chars")
            assertTrue("Transcript should not be blank", text.isNotBlank())
        } else {
            val exception = result.exceptionOrNull()
            val msg = exception?.message ?: ""
            println("❌ FAILURE!")
            println("⚠️ Reason: $msg")
            
            assertTrue("Error fetching transcript: $msg", false)
        }
    }
}
