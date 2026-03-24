package com.skul9x.ytsummary.repository

import android.content.Context
import com.skul9x.ytsummary.api.GeminiApiClient
import com.skul9x.ytsummary.di.NetworkModule
import com.skul9x.ytsummary.model.AiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository điều phối luồng: Backend (Transcript) -> Google AI (Summarize).
 */
class SummarizationRepository(context: Context) {
    
    private val db = com.skul9x.ytsummary.data.AppDatabase.getDatabase(context)
    private val summaryDao = db.summaryDao()
    private val backendApi = NetworkModule.api
    private val geminiApi = GeminiApiClient(
        apiKeyManager = com.skul9x.ytsummary.manager.ApiKeyManager.getInstance(context),
        quotaManager = com.skul9x.ytsummary.manager.ModelQuotaManager(context)
    )

    /**
     * Lấy Metadata (Title, Thumbnail) của video.
     */
    fun getVideoMetadata(videoId: String): Flow<com.skul9x.ytsummary.model.VideoMetadata?> = flow {
        try {
            val response = backendApi.getMetadata(videoId)
            if (response.isSuccessful) {
                emit(response.body())
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    /**
     * Thực hiện tóm tắt video. Trích xuất transcript từ Backend sau đó tóm tắt qua Gemini.
     */
    fun getSummary(videoId: String, videoTitle: String = "", thumbnailUrl: String = ""): Flow<AiResult> = flow {
        // 1. Lấy Transcript từ Backend
        val transcriptResponse = try {
            backendApi.getTranscript(videoId)
        } catch (e: Exception) {
            emit(AiResult.Error("Lỗi kết nối Backend: ${e.message}"))
            return@flow
        }

        if (!transcriptResponse.isSuccessful) {
            emit(AiResult.Error("Backend error: ${transcriptResponse.code()}"))
            return@flow
        }

        val transcript = transcriptResponse.body()?.transcript
        if (transcript.isNullOrBlank()) {
            emit(AiResult.Error("Không lấy được nội dung phụ đề"))
            return@flow
        }

        // 2. Tóm tắt bằng Gemini (đã có xoay tua Key bên trong)
        val result = geminiApi.summarize(transcript)
        
        // 3. Nếu thành công -> Lưu vào History
        if (result is AiResult.Success) {
            summaryDao.insertSummary(
                com.skul9x.ytsummary.data.SummaryEntity(
                    videoId = videoId,
                    title = videoTitle,
                    thumbnailUrl = thumbnailUrl,
                    summaryText = result.text
                )
            )
        }
        
        emit(result)
    }

    /**
     * Lấy danh sách lịch sử tóm tắt.
     */
    fun getAllHistory(): Flow<List<com.skul9x.ytsummary.data.SummaryEntity>> = summaryDao.getAllSummaries()

    /**
     * Xóa 1 mục lịch sử.
     */
    suspend fun deleteHistoryItem(videoId: String) = summaryDao.deleteByVideoId(videoId)

    /**
     * Xóa toàn bộ lịch sử.
     */
    suspend fun clearAllHistory() = summaryDao.clearHistory()
}
