package com.skul9x.ytsummary.transcript

import android.text.Html
import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Test tích hợp (Integration Test) để kiểm tra khả năng lấy phụ đề thực tế từ YouTube.
 * Video ID: MbOah3Fjkhc
 */
class YouTubeTranscriptIntegrationTest {

    private lateinit var service: YouTubeTranscriptService
    private val videoId = "MbOah3Fjkhc"

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any()) } returns 0

        // Mock Html.fromHtml
        mockkStatic(Html::class)
        val mockedSpanned = io.mockk.mockk<android.text.Spanned>()
        every { mockedSpanned.toString() } returns "Mocked Transcript Text"
        every { Html.fromHtml(any<String>()) } returns mockedSpanned

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        service = YouTubeTranscriptService(client)
    }

    @Test
    fun `test fetch transcript from real video`() = runBlocking {
        println("🚀 Đang thử lấy phụ đề cho video: $videoId...")
        
        val result = service.fetchTranscript(videoId, listOf("vi", "en"))
        
        if (result.isSuccess) {
            val text = result.getOrNull() ?: ""
            println("✅ THÀNH CÔNG!")
            println("📝 Độ dài phụ đề: ${text.length} ký tự")
            println("📄 Đoạn đầu phụ đề: ${text.take(200)}...")
            
            assertTrue("Phụ đề không được rỗng", text.isNotBlank())
        } else {
            val exception = result.exceptionOrNull()
            println("❌ THẤB BẠI!")
            println("⚠️ Lý do: ${exception?.message}")
            println("🔍 Loại lỗi: ${exception?.javaClass?.simpleName}")
            
            // In stacktrace để debug nếu cần
            exception?.printStackTrace()
            
            assertTrue("Lỗi khi lấy phụ đề: ${exception?.message}", false)
        }
    }
}
