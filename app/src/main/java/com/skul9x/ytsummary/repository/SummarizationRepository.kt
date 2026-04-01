package com.skul9x.ytsummary.repository

import android.content.Context
import android.util.Log
import com.skul9x.ytsummary.api.GeminiApiClient
import com.skul9x.ytsummary.di.NetworkModule
import com.skul9x.ytsummary.model.AiResult
import com.skul9x.ytsummary.model.VideoMetadata
import com.skul9x.ytsummary.transcript.MetadataService
import com.skul9x.ytsummary.transcript.OEmbedMetadataService
import com.skul9x.ytsummary.transcript.TranscriptService
import com.skul9x.ytsummary.transcript.YouTubeTranscriptService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext

/**
 * Repository điều phối luồng: Transcript (Native Kotlin) -> Google AI (Summarize).
 * 
 * Migration v5.0: Thay PythonManager bằng native TranscriptService + MetadataService.
 */
class SummarizationRepository private constructor(context: Context) {
    
    companion object {
        private const val TAG = "SummarizationRepo"

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
    private val transcriptCache = com.skul9x.ytsummary.data.TranscriptCache(context)

    // Native Kotlin services (thay thế PythonManager)
    private val transcriptService: TranscriptService = YouTubeTranscriptService(NetworkModule.okHttpClient)
    private val metadataService: MetadataService = OEmbedMetadataService(NetworkModule.okHttpClient)

    private val geminiApi = GeminiApiClient(
        apiKeyManager = com.skul9x.ytsummary.manager.ApiKeyManager.getInstance(context),
        quotaManager = com.skul9x.ytsummary.manager.ModelQuotaManager.getInstance(context),
        modelManager = com.skul9x.ytsummary.manager.ModelManager.getInstance(context)
    )

    /**
     * Lấy Metadata (Title, Thumbnail) của video qua oEmbed API (native Kotlin).
     */
    fun getVideoMetadata(videoId: String): Flow<VideoMetadata?> = flow {
        try {
            val metadata = com.skul9x.ytsummary.util.retryWithBackoff(
                maxRetries = 3,
                tag = TAG,
                shouldRetry = { it is java.io.IOException || (it is com.skul9x.ytsummary.transcript.exception.TranscriptException.HttpError && it.statusCode >= 500) }
            ) {
                metadataService.fetchMetadata(videoId)
            }
            emit(metadata)
        } catch (e: Exception) {
            Log.e(TAG, "Metadata error for $videoId after retries: ${e.message}")
            emit(null)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO)

    /**
     * Thực hiện tóm tắt video. Trích xuất transcript (native Kotlin) sau đó tóm tắt qua Gemini.
     */
    fun getSummary(videoId: String): Flow<AiResult> = flow {
        // Check cache first
        val cached = summaryDao.getSummaryById(videoId)
        if (cached != null) {
            emit(AiResult.Success(cached.summaryText, "cache"))
            return@flow
        }

        // 1. Lấy Transcript (Check cache trước)
        var transcript = transcriptCache.get(videoId)
        
        if (transcript == null) {
            emit(AiResult.Loading("📺 Đang lấy phụ đề..."))
            
            val transcriptResult = com.skul9x.ytsummary.util.retryWithBackoff(
                maxRetries = 3,
                tag = TAG,
                shouldRetry = { it is java.io.IOException || (it is com.skul9x.ytsummary.transcript.exception.TranscriptException.HttpError && it.statusCode >= 500) }
            ) {
                transcriptService.fetchTranscript(videoId)
            }
            
            // Guard: Kiểm tra coroutine vẫn active
            currentCoroutineContext().ensureActive()
            
            if (transcriptResult.isFailure) {
                emit(AiResult.Error("Lỗi lấy phụ đề: ${transcriptResult.exceptionOrNull()?.message}"))
                return@flow
            }

            transcript = transcriptResult.getOrNull()
            if (transcript.isNullOrBlank()) {
                emit(AiResult.Error("Không lấy được nội dung phụ đề"))
                return@flow
            }
            
            // Lưu vào cache để lần sau dùng luôn
            transcriptCache.save(videoId, transcript)
        } else {
            Log.d(TAG, "Using cached transcript for $videoId")
        }

        // 2. Tóm tắt bằng Gemini
        emit(AiResult.Loading("🤖 AI Gemini đang đọc và phân tích..."))
        geminiApi.summarize(transcript).collect { result ->
            emit(result)
        }
    }.flowOn(kotlinx.coroutines.Dispatchers.IO) 

    /**
     * Lưu lịch sử tóm tắt xuống DB.
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
     * Lấy danh sách lịch sử tóm tắt (hỗ trợ Paging 3).
     * Chỉ lấy các trường cơ bản (videoId, title, thumbnailUrl, timestamp).
     */
    fun getHistoryItems(): androidx.paging.PagingSource<Int, com.skul9x.ytsummary.model.HistoryItem> = summaryDao.getHistoryItems()

    /**
     * Lấy danh sách lịch sử tóm tắt (hỗ trợ Paging 3).
     */
    fun getAllHistory(): androidx.paging.PagingSource<Int, com.skul9x.ytsummary.data.SummaryEntity> = summaryDao.getAllSummaries()

    /**
     * Lấy 1 bản tóm tắt cụ thể theo videoId.
     */
    suspend fun getSummaryById(videoId: String): com.skul9x.ytsummary.data.SummaryEntity? = summaryDao.getSummaryById(videoId)

    /**
     * Xóa 1 mục lịch sử.
     */
    suspend fun deleteHistoryItem(videoId: String) = summaryDao.deleteByVideoId(videoId)

    /**
     * Lấy tổng số bản tóm tắt hiện có.
     */
    fun getHistoryCount(): Flow<Int> = summaryDao.getSummaryCount()

    /**
     * Xóa toàn bộ lịch sử.
     */
    suspend fun clearAllHistory() = summaryDao.clearHistory()
}
