package com.skul9x.ytsummary.repository

import android.content.Context
import com.skul9x.ytsummary.api.GeminiApiClient
import com.skul9x.ytsummary.di.NetworkModule
import com.skul9x.ytsummary.model.AiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext

/**
 * Repository điều phối luồng: Backend (Transcript) -> Google AI (Summarize).
 */
class SummarizationRepository private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var instance: SummarizationRepository? = null

        fun getInstance(context: Context): SummarizationRepository {
            return instance ?: synchronized(this) {
                instance ?: SummarizationRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val db = com.skul9x.ytsummary.data.AppDatabase.getDatabase(context)
    private val summaryDao = db.summaryDao()
    private val pythonManager = com.skul9x.ytsummary.manager.PythonManager.getInstance(context)
    private val geminiApi = GeminiApiClient(
        apiKeyManager = com.skul9x.ytsummary.manager.ApiKeyManager.getInstance(context),
        quotaManager = com.skul9x.ytsummary.manager.ModelQuotaManager.getInstance(context),
        modelManager = com.skul9x.ytsummary.manager.ModelManager.getInstance(context)
    )

    /**
     * Lấy Metadata (Title, Thumbnail) của video locally qua Python.
     */
    fun getVideoMetadata(videoId: String): Flow<com.skul9x.ytsummary.model.VideoMetadata?> = flow {
        try {
            val metadata = pythonManager.fetchMetadata(videoId)
            emit(metadata)
        } catch (e: Exception) {
            emit(null)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO) // Fixed C3: Runs on background thread

    /**
     * Thực hiện tóm tắt video. Trích xuất transcript locally sau đó tóm tắt qua Gemini.
     */
    fun getSummary(videoId: String): Flow<AiResult> = flow {
        // Check cache first
        val cached = summaryDao.getSummaryById(videoId)
        if (cached != null) {
            emit(AiResult.Success(cached.summaryText, "cache"))
            return@flow
        }

        emit(AiResult.Loading("📺 Đang lọc phụ đề qua Python..."))
        
        // 1. Lấy Transcript locally via Python (Safe Threading due to flowOn)
        val transcriptResult = pythonManager.fetchTranscript(videoId)
        
        // Guard: Kiểm tra coroutine vẫn active sau khi blocking call Python trả về
        currentCoroutineContext().ensureActive()
        
        if (transcriptResult.isFailure) {
            emit(AiResult.Error("Lỗi lấy phụ đề (Local): ${transcriptResult.exceptionOrNull()?.message}"))
            return@flow
        }

        val transcript = transcriptResult.getOrNull()
        if (transcript.isNullOrBlank()) {
            emit(AiResult.Error("Không lấy được nội dung phụ đề"))
            return@flow
        }

        // 2. Tóm tắt bằng Gemini
        emit(AiResult.Loading("🤖 AI Gemini đang đọc và phân tích..."))
        geminiApi.summarize(transcript).collect { result ->
            emit(result)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO) 

    /**
     * Lưu lịch sử tóm tắt xuống DB (Gọi từ ViewModel/Activity sau khi đã có cả Metadata và Summary)
     */
    suspend fun saveToHistory(videoId: String, title: String, thumbnailUrl: String, summaryText: String) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            summaryDao.insertSummary(
                com.skul9x.ytsummary.data.SummaryEntity(
                    videoId = videoId,
                    title = title,
                    thumbnailUrl = thumbnailUrl,
                    summaryText = summaryText
                )
            )
        }
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
