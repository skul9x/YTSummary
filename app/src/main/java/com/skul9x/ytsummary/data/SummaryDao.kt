package com.skul9x.ytsummary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Interface truy vấn dữ liệu cho bảng summaries.
 */
@Dao
interface SummaryDao {

    @Query("SELECT * FROM summaries ORDER BY timestamp DESC")
    fun getAllSummaries(): Flow<List<SummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: String)

    @Query("DELETE FROM summaries")
    suspend fun clearHistory()
}
