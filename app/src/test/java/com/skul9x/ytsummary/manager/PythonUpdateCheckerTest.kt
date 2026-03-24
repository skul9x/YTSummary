package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import com.skul9x.ytsummary.di.NetworkModule
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Test cho PythonUpdateChecker sử dụng MockWebServer.
 * Cách này tránh việc phải mock OkHttpClient (thường bị lỗi với final classes).
 */
class PythonUpdateCheckerTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        
        // Mock NetworkModule.okHttpClient để nó gọi vào server giả
        // Thay vì mock OkHttpClient, chúng ta mock NetworkModule.okHttpClient 
        // trả về một client thật (vì client thật chơi được với MockWebServer)
        mockkObject(NetworkModule)
        
        // Reset test version
        PythonUpdateChecker.testVersion = null

        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor
    }

    @After
    fun tearDown() {
        server.shutdown()
        unmockkAll()
        PythonUpdateChecker.testVersion = null
        PythonUpdateChecker.PYPI_URL = "https://pypi.org/pypi/youtube-transcript-api/json"
    }

    @Test
    fun `checkForUpdate returns null when versions match`() = runBlocking {
        mockPythonVersion("0.6.2")
        
        // Giả lập PyPI response 0.6.2
        server.enqueue(MockResponse().setBody("{\"info\":{\"version\":\"0.6.2\"}}"))
        
        // Patch URL trong object để gọi vào localhost
        PythonUpdateChecker.PYPI_URL = server.url("/").toString()

        val result = PythonUpdateChecker.checkForUpdate(context)
        assertNull(result)
    }

    @Test
    fun `checkForUpdate returns UpdateInfo when remote version is newer`() = runBlocking {
        mockPythonVersion("0.6.2")
        
        // Giả lập PyPI response 0.6.3
        server.enqueue(MockResponse().setBody("{\"info\":{\"version\":\"0.6.3\"}}"))
        
        // Patch URL trong object để gọi vào localhost
        PythonUpdateChecker.PYPI_URL = server.url("/").toString()

        val result = PythonUpdateChecker.checkForUpdate(context)

        assertNotNull(result)
        assertEquals("0.6.2", result?.currentVersion)
        assertEquals("0.6.3", result?.latestVersion)
    }

    @Test
    fun `checkForUpdate returns cached version when cache is valid`() = runBlocking {
        mockPythonVersion("0.6.2")

        // Mock cache (Valid cache: 0.6.4)
        every { sharedPrefs.getLong("cache_timestamp", 0) } returns System.currentTimeMillis()
        every { sharedPrefs.getString("cached_latest_version", null) } returns "0.6.4"

        val result = PythonUpdateChecker.checkForUpdate(context)

        // Không gọi network (server.requestCount == 0)
        assertEquals(0, server.requestCount)
        assertNotNull(result)
        assertEquals("0.6.4", result?.latestVersion)
    }

    private fun mockPythonVersion(version: String) {
        PythonUpdateChecker.testVersion = version
    }
}
