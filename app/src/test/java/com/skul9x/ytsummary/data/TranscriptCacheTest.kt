package com.skul9x.ytsummary.data

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TranscriptCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var cache: TranscriptCache
    private lateinit var mockCacheDir: File

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        
        context = mockk()
        mockCacheDir = tempFolder.newFolder("cache")
        every { context.cacheDir } returns mockCacheDir
        cache = TranscriptCache(context)
    }

    @Test
    fun testSaveAndGet() {
        val videoId = "test_video_123"
        val transcript = "Hello world transcript"
        
        cache.save(videoId, transcript)
        val retrieved = cache.get(videoId)
        
        assertEquals(transcript, retrieved)
    }

    @Test
    fun testGetNonExistent() {
        assertNull(cache.get("none"))
    }

    @Test
    fun testCacheExpiry() {
        val videoId = "expired_video"
        val transcript = "Old transcript"
        
        cache.save(videoId, transcript)
        
        val transcriptsDir = File(mockCacheDir, "transcripts")
        val file = File(transcriptsDir, videoId)
        assertTrue("File should exist after save", file.exists())
        
        // Set lastModified to 25 hours ago
        val oldTime = System.currentTimeMillis() - (25 * 60 * 60 * 1000L)
        file.setLastModified(oldTime)
        
        val retrieved = cache.get(videoId)
        assertNull("Should be null because it expired", retrieved)
        assertFalse("File should be deleted after expiry check", file.exists())
    }
}
