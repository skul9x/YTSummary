package com.skul9x.ytsummary.network

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0

        server = MockWebServer()
        server.start()
        
        client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun testRetryOn500_EventuallySucceeds() {
        // Gửi 2 lỗi 500, lần thứ 3 thành công 200
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        val request = Request.Builder().url(server.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals("Success", response.body?.string())
        assertEquals(3, server.requestCount) // Gốc + 2 lần retry
    }

    @Test
    fun testNoRetryOn404() {
        // Lỗi 4xx không được retry
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        val request = Request.Builder().url(server.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(404, response.code)
        assertEquals(1, server.requestCount) 
    }

    @Test
    fun testNoRetryOn429() {
        // Lỗi 429 không retry ở tầng này (để GeminiApiClient xử lý đổi model)
        server.enqueue(MockResponse().setResponseCode(429).setBody("Too Many Requests"))

        val request = Request.Builder().url(server.url("/")).build()
        val response = client.newCall(request).execute()

        assertEquals(429, response.code)
        assertEquals(1, server.requestCount)
    }
}
