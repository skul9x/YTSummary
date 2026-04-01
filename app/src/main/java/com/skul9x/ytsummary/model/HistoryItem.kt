package com.skul9x.ytsummary.model

/**
 * DTO đại diện cho một mục trong danh sách lịch sử.
 * Không chứa summaryText để tối ưu hóa bộ nhớ và I/O.
 */
data class HistoryItem(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val timestamp: Long
)
