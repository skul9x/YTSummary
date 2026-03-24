package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.model.TranscriptResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface giao tiếp với YouTube Summarizer Backend proxy.
 * Chỉ có một endpoint duy nhất phục vụ việc lấy transcript.
 */
interface YouTubeApiClient {

    /**
     * Lấy phụ đề đã làm sạch từ Backend.
     * @param videoId ID của video YouTube (ví dụ: dQw4w9WgXcQ)
     */
    @GET("api/transcript")
    suspend fun getTranscript(
        @Query("video_id") videoId: String
    ): Response<TranscriptResponse>
    
    /**
     * Lấy thông tin Metadata (Title, Thumbnail) từ Backend.
     */
    @GET("api/metadata")
    suspend fun getMetadata(
        @Query("video_id") videoId: String
    ): Response<com.skul9x.ytsummary.model.VideoMetadata>

    /**
     * Kiểm tra trạng thái Backend
     */
    @GET("/")
    suspend fun checkHealth(): Response<Map<String, String>>
}
