package com.skul9x.ytsummary.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RetryUtilsTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>()) } returns 0
    }

    @Test
    fun `retryWithBackoff should return value if first call succeeds`() = runBlocking {
        var calls = 0
        val result = retryWithBackoff(maxRetries = 3, initialDelayMillis = 1) {
            calls++
            "success"
        }
        assertEquals("success", result)
        assertEquals(1, calls)
    }

    @Test
    fun `retryWithBackoff should retry on failure and eventually succeed`() = runBlocking {
        var calls = 0
        val result = retryWithBackoff(maxRetries = 3, initialDelayMillis = 1) {
            calls++
            if (calls < 3) throw IOException("Failure")
            "success"
        }
        assertEquals("success", result)
        assertEquals(3, calls)
    }

    @Test
    fun `retryWithBackoff should throw exception after max retries`() = runBlocking {
        var calls = 0
        try {
            retryWithBackoff(maxRetries = 3, initialDelayMillis = 1) {
                calls++
                throw IOException("Permanent Failure")
            }
            fail("Should have thrown exception")
        } catch (e: IOException) {
            assertEquals("Permanent Failure", e.message)
            assertEquals(4, calls) // Initial attempt + 3 retries
        }
    }
}
