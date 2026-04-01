package com.skul9x.ytsummary.transcript

import android.util.Log
import com.skul9x.ytsummary.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Implementation lấy metadata qua YouTube oEmbed API.
 * Port từ Python get_metadata() trong yt_transcript_helper.py.
 *
 * oEmbed API: Không cần API key, trả về title, thumbnail, author.
 * Fallback: nếu lỗi, trả về metadata tối thiểu (thumbnail từ i.ytimg.com).
 */
class OEmbedMetadataService(
    private val okHttpClient: OkHttpClient
) : MetadataService {

    companion object {
        private const val TAG = "OEmbedMetadata"
        private const val OEMBED_URL =
            "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=%s&format=json"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchMetadata(videoId: String): VideoMetadata? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(String.format(OEMBED_URL, videoId))
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext fallback(videoId)
                    val data = json.parseToJsonElement(body).jsonObject

                    VideoMetadata(
                        videoId = videoId,
                        title = data["title"]?.jsonPrimitive?.content ?: "Unknown Title",
                        thumbnailUrl = data["thumbnail_url"]?.jsonPrimitive?.content ?: "",
                        authorName = data["author_name"]?.jsonPrimitive?.content ?: "",
                        status = "success"
                    )
                } else {
                    Log.w(TAG, "oEmbed failed for $videoId: HTTP ${response.code}")
                    fallback(videoId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Metadata error for $videoId: ${e.message}", e)
                fallback(videoId)
            }
        }

    /**
     * Fallback metadata khi oEmbed API fail.
     * Giống Python: trả về thumbnail từ i.ytimg.com.
     */
    private fun fallback(videoId: String) = VideoMetadata(
        videoId = videoId,
        title = "Video $videoId",
        thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
        authorName = "",
        status = "fallback"
    )
}
