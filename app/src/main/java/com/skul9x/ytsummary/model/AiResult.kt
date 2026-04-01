package com.skul9x.ytsummary.model

/**
 * Các trạng thái kết quả khi gọi AI API.
 */
sealed class AiResult {
    data class Loading(val message: String) : AiResult()
    data class Success(val text: String, val model: String) : AiResult()
    data class DeltaSuccess(val chunk: String, val model: String) : AiResult()
    data object QuotaExceeded : AiResult()
    data object ServerBusy : AiResult()
    data object NoApiKeys : AiResult()
    data object AllQuotaExhausted : AiResult()
    data class Error(val message: String) : AiResult()
}
