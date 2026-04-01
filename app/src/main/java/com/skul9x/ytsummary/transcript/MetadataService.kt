package com.skul9x.ytsummary.transcript

import com.skul9x.ytsummary.model.VideoMetadata

/**
 * Interface cho dịch vụ lấy metadata video YouTube.
 * Clean Architecture: tách biệt khỏi implementation.
 */
interface MetadataService {

    /**
     * Lấy metadata (title, thumbnail, author) cho video.
     *
     * @param videoId YouTube video ID
     * @return VideoMetadata hoặc null nếu lỗi (fallback)
     */
    suspend fun fetchMetadata(videoId: String): VideoMetadata?
}
