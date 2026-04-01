package com.skul9x.ytsummary.transcript.model

/**
 * Một đoạn (snippet) trong transcript.
 * Tương đương FetchedTranscriptSnippet trong Python.
 */
data class TranscriptSnippet(
    /** Nội dung text đã được clean (HTML tags removed, entities unescaped) */
    val text: String,
    /** Thời điểm bắt đầu hiển thị (giây) */
    val start: Float,
    /** Thời gian hiển thị trên màn hình (giây). Có thể overlap giữa các snippet. */
    val duration: Float
)
