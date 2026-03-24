package com.skul9x.ytsummary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity lưu trữ bản tóm tắt video.
 */
@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val title: String,
    val thumbnailUrl: String,
    val summaryText: String,
    val timestamp: Long = System.currentTimeMillis()
)
