package com.skul9x.ytsummary.model

import com.google.gson.annotations.SerializedName

/**
 * Model chứa thông tin cơ bản của video YouTube.
 */
data class VideoMetadata(
    @SerializedName("video_id") val videoId: String,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("status") val status: String
)
