package com.skul9x.ytsummary.transcript.exception

/**
 * Sealed class hierarchy cho tất cả lỗi liên quan đến transcript.
 * Port từ Python youtube-transcript-api._errors với messages tiếng Việt.
 */
sealed class TranscriptException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /** Video đã tắt phụ đề (captions disabled) */
    class TranscriptsDisabled(val videoId: String) :
        TranscriptException("Video $videoId đã tắt phụ đề.")

    /** Không tìm thấy phụ đề cho ngôn ngữ yêu cầu */
    class NoTranscriptFound(
        val videoId: String,
        val requestedLanguages: List<String>
    ) : TranscriptException(
        "Không tìm thấy phụ đề phù hợp (${requestedLanguages.joinToString("/")}) cho video $videoId."
    )

    /** Video không tồn tại hoặc đã bị xóa */
    class VideoUnavailable(val videoId: String) :
        TranscriptException("Video $videoId không tồn tại hoặc bị ẩn.")

    /** Truyền URL thay vì video ID */
    class InvalidVideoId(val videoId: String) :
        TranscriptException("ID video không hợp lệ. Hãy dùng video ID (11 ký tự), không phải URL đầy đủ.")

    /** YouTube chặn IP (HTTP 429 hoặc recaptcha) */
    class IpBlocked(val videoId: String) :
        TranscriptException("YouTube đang chặn request từ IP này. Thử lại sau.")

    /** YouTube phát hiện bot */
    class RequestBlocked(val videoId: String) :
        TranscriptException("YouTube đang chặn request. Thử lại sau.")

    /** Video bị giới hạn tuổi, cần xác thực */
    class AgeRestricted(val videoId: String) :
        TranscriptException("Video $videoId bị giới hạn tuổi, không thể lấy phụ đề.")

    /** Video không phát được vì lý do khác */
    class VideoUnplayable(
        val videoId: String,
        val reason: String?,
        val subReasons: List<String> = emptyList()
    ) : TranscriptException(
        buildString {
            append("Video $videoId không phát được")
            if (reason != null) append(": $reason")
            if (subReasons.isNotEmpty()) {
                append("\n")
                subReasons.forEach { append("  - $it\n") }
            }
        }
    )

    /** Cần PO Token để truy cập transcript */
    class PoTokenRequired(val videoId: String) :
        TranscriptException("Video $videoId yêu cầu PO Token, không thể lấy phụ đề.")

    /** Lỗi HTTP chung khi gọi YouTube */
    class HttpError(
        val videoId: String,
        val statusCode: Int,
        override val cause: Throwable? = null
    ) : TranscriptException("Lỗi HTTP $statusCode khi lấy dữ liệu video $videoId.", cause)

    /** Không tạo được consent cookie (EU) */
    class ConsentRequired(val videoId: String) :
        TranscriptException("Không thể xử lý trang đồng ý cookie cho video $videoId.")

    /** Dữ liệu từ YouTube không thể parse */
    class DataUnparsable(val videoId: String) :
        TranscriptException("Không thể phân tích dữ liệu YouTube cho video $videoId.")

    /** Lỗi kết nối mạng */
    class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : TranscriptException(message, cause)
}
