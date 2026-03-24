package com.skul9x.ytsummary.model

import com.google.gson.annotations.SerializedName

/**
 * Model đại diện cho phản hồi từ Backend API (/api/transcript)
 */
data class TranscriptResponse(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("length")
    val length: Int,
    
    @SerializedName("transcript")
    val transcript: String
)
