package com.skul9x.ytsummary.transcript

import com.skul9x.ytsummary.transcript.model.TranscriptResult

/**
 * Interface cho dịch vụ lấy transcript từ YouTube.
 * Clean Architecture: tách biệt interface khỏi implementation.
 * Có thể swap sang Invidious, Piped, hoặc YouTube Data API v3 sau này.
 */
interface TranscriptService {

    /**
     * Lấy transcript dạng plain text cho video.
     * Ưu tiên ngôn ngữ theo thứ tự [languages], manual trước, generated sau.
     *
     * @param videoId YouTube video ID (11 ký tự)
     * @param languages Danh sách mã ngôn ngữ ưu tiên, e.g. ["vi", "en"]
     * @return Result.success(plainText) hoặc Result.failure(TranscriptException)
     */
    suspend fun fetchTranscript(
        videoId: String,
        languages: List<String> = listOf("vi", "en")
    ): Result<String>

    /**
     * Lấy transcript đầy đủ bao gồm metadata (snippets, language info).
     *
     * @param videoId YouTube video ID (11 ký tự)
     * @param languages Danh sách mã ngôn ngữ ưu tiên
     * @return Result.success(TranscriptResult) hoặc Result.failure(TranscriptException)
     */
    suspend fun fetchTranscriptResult(
        videoId: String,
        languages: List<String> = listOf("vi", "en")
    ): Result<TranscriptResult>
}
