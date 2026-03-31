package com.skul9x.ytsummary.transcript.model

/**
 * Thông tin một caption track từ YouTube InnerTube API.
 * Tương đương dữ liệu trong captions.playerCaptionsTracklistRenderer.captionTracks[].
 */
data class CaptionTrack(
    /** URL để fetch transcript XML (đã bỏ &fmt=srv3) */
    val baseUrl: String,
    /** Tên ngôn ngữ đầy đủ, e.g. "English", "Vietnamese" */
    val language: String,
    /** Mã ngôn ngữ, e.g. "en", "vi" */
    val languageCode: String,
    /** True nếu là phụ đề tự động (kind == "asr") */
    val isGenerated: Boolean,
    /** True nếu track có thể dịch sang ngôn ngữ khác */
    val isTranslatable: Boolean
)
