package com.skul9x.ytsummary.transcript.model

/**
 * Kết quả đầy đủ của một transcript đã fetch thành công.
 * Tương đương FetchedTranscript trong Python.
 */
data class TranscriptResult(
    /** Danh sách các snippet trong transcript */
    val snippets: List<TranscriptSnippet>,
    /** Tên ngôn ngữ, e.g. "Vietnamese" */
    val language: String,
    /** Mã ngôn ngữ, e.g. "vi" */
    val languageCode: String,
    /** True nếu là phụ đề tự động (auto-generated) */
    val isGenerated: Boolean,
    /** Video ID */
    val videoId: String
) {
    /**
     * Nối tất cả snippet thành plain text, bỏ timestamp.
     * Giữ nguyên khoảng trắng giữa các snippet (tương tự Python: " ".join).
     */
    fun toPlainText(): String =
        snippets.joinToString(" ") { it.text }
            .replace("\n", " ")
            .trim()
}
