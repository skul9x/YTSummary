package com.skul9x.ytsummary.manager

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.skul9x.ytsummary.model.VideoMetadata

/**
 * Bridge between Kotlin and local Python scripts (using Chaquopy).
 * 
 * IMPORTANT: Chaquopy's PyObject.get() uses Python getattr() (attribute access).
 * For Python dicts, we must use PyObject.asMap().get() which uses __getitem__ (dict key access).
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
            val result = ytHelper.callAttr("get_transcript", videoId)
            
            // MUST use asMap() to read Python dict keys! .get() uses getattr which won't work for dicts.
            val resultMap = result.asMap()
            val status = resultMap[PyObject.fromJava("status")]?.toString()
            
            Log.d(TAG, "fetchTranscript status=$status")
            
            if (status == "success") {
                val transcript = resultMap[PyObject.fromJava("transcript")]?.toString() ?: ""
                Log.d(TAG, "Transcript fetched OK, length=${transcript.length}")
                Result.success(transcript)
            } else {
                val message = resultMap[PyObject.fromJava("message")]?.toString() ?: "Lỗi không xác định từ Python"
                Log.w(TAG, "Python error: $message")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            val rootCause = e.cause?.message ?: e.message ?: "Không rõ lỗi"
            Log.e(TAG, "fetchTranscript crash: $rootCause", e)
            Result.failure(Exception("Lỗi Python runtime: $rootCause"))
        }
    }

    /**
     * Fetch video metadata locally via Python.
     */
    fun fetchMetadata(videoId: String): VideoMetadata? {
        return try {
            val result = ytHelper.callAttr("get_metadata", videoId)
            val m = result.asMap()
            
            VideoMetadata(
                videoId = videoId,
                title = m[PyObject.fromJava("title")]?.toString() ?: "Unknown",
                thumbnailUrl = m[PyObject.fromJava("thumbnail_url")]?.toString() ?: "",
                authorName = m[PyObject.fromJava("author_name")]?.toString() ?: "",
                status = m[PyObject.fromJava("status")]?.toString() ?: "fallback"
            )
        } catch (e: Exception) {
            Log.e(TAG, "fetchMetadata error: ${e.message}", e)
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
