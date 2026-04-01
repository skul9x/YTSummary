package com.skul9x.ytsummary.data

import android.content.Context
import android.util.Log
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Lớp cache lưu trữ transcript text xuống FileSystem để tái sử dụng, 
 * giúp giảm tải cho Python fetch (thường mất 3-10s).
 * TTL mặc định: 24 giờ.
 */
class TranscriptCache(context: Context) {
    private val cacheDir = File(context.cacheDir, "transcripts")

    init {
        if (!cacheDir.exists()) {
            if (cacheDir.mkdirs()) {
                Log.d(TAG, "Transcript cache directory created: ${cacheDir.absolutePath}")
            }
        }
    }

    /**
     * Lưu transcript vào cache.
     */
    fun save(videoId: String, transcript: String) {
        try {
            val file = File(cacheDir, videoId)
            file.writeText(transcript)
            Log.d(TAG, "Transcript saved to cache for videoId: $videoId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save transcript to cache: ${e.message}")
        }
    }

    /**
     * Lấy transcript từ cache nếu còn hạn (24h).
     */
    fun get(videoId: String): String? {
        val file = File(cacheDir, videoId)
        if (!file.exists()) return null

        val lastModified = file.lastModified()
        val now = System.currentTimeMillis()

        // Kiểm tra TTL (24 giờ)
        if (now - lastModified > TimeUnit.HOURS.toMillis(24)) {
            Log.d(TAG, "Cache expired for videoId: $videoId. Deleting...")
            file.delete()
            return null
        }

        return try {
            val content = file.readText()
            Log.d(TAG, "Transcript hit cache for videoId: $videoId")
            content
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read transcript from cache: ${e.message}")
            null
        }
    }

    /**
     * Xóa toàn bộ cache.
     */
    fun clear() {
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    companion object {
        private const val TAG = "TranscriptCache"
    }
}
