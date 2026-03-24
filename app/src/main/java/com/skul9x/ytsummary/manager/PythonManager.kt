package com.skul9x.ytsummary.manager

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.skul9x.ytsummary.model.VideoMetadata

/**
 * Bridge between Kotlin and local Python scripts (using Chaquopy).
 */
class PythonManager private constructor(context: Context) {

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context.applicationContext))
        }
    }

    private val python = Python.getInstance()
    private val ytHelper = python.getModule("yt_transcript_helper")

    /**
     * Fetch transcript locally via Python.
     */
    fun fetchTranscript(videoId: String): Result<String> {
        return try {
            val result = ytHelper.callAttr("get_transcript", videoId).toPyObject()
            val status = result.get("status")?.toString() ?: "error"
            
            if (status == "success") {
                val transcript = result.get("transcript")?.toString() ?: ""
                Result.success(transcript)
            } else {
                val message = result.get("message")?.toString() ?: "Unknown python error"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Python fetchTranscript error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch video metadata locally via Python.
     */
    fun fetchMetadata(videoId: String): VideoMetadata? {
        return try {
            val result = ytHelper.callAttr("get_metadata", videoId).toPyObject()
            
            VideoMetadata(
                videoId = videoId,
                title = result.get("title")?.toString() ?: "Unknown",
                thumbnailUrl = result.get("thumbnail_url")?.toString() ?: "",
                authorName = result.get("author_name")?.toString() ?: "",
                status = result.get("status")?.toString() ?: "fallback"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Python fetchMetadata error: ${e.message}", e)
            null
        }
    }

    companion object {
        private const val TAG = "PythonManager"

        @Volatile
        private var instance: PythonManager? = null

        fun getInstance(context: Context): PythonManager {
            return instance ?: synchronized(this) {
                instance ?: PythonManager(context).also { instance = it }
            }
        }
    }
}
