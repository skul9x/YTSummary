package com.skul9x.ytsummary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skul9x.ytsummary.model.AiResult
import com.skul9x.ytsummary.repository.SummarizationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Main : ScreenState()
    object Settings : ScreenState()
    object History : ScreenState()
    data class Loading(val message: String = "Đang chuẩn bị...") : ScreenState()
    data class Summary(val result: AiResult) : ScreenState()
}

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SummarizationRepository.getInstance(application)

    // --- UI State ---
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Main)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _videoTitle = MutableStateFlow("Summarizing...")
    val videoTitle: StateFlow<String> = _videoTitle.asStateFlow()

    private val _thumbnailUrl = MutableStateFlow("")
    val thumbnailUrl: StateFlow<String> = _thumbnailUrl.asStateFlow()

    // Auto-Read Pending state flag
    private val _autoReadPending = MutableStateFlow(false)
    val autoReadPending: StateFlow<Boolean> = _autoReadPending.asStateFlow()
    
    // TTS State
    private val _isTtsPlaying = MutableStateFlow(false)
    val isTtsPlaying: StateFlow<Boolean> = _isTtsPlaying.asStateFlow()

    private var ttsPausedIndex = 0

    private var summaryJob: Job? = null

    fun summarize(url: String) {
        val videoId = extractVideoId(url) ?: return
        
        // Cancel job cũ nếu đang chạy
        summaryJob?.cancel()
        resetTtsPausedIndex()
        setTtsPlaying(false)
        _screenState.value = ScreenState.Loading("📺 Đang lọc phụ đề...")

        summaryJob = viewModelScope.launch {
            // 1. Metadata song song
            val metadataJob = async {
                repository.getVideoMetadata(videoId)
                    .collect { metadata ->
                        if (metadata != null) {
                            _videoTitle.value = metadata.title
                            _thumbnailUrl.value = metadata.thumbnailUrl
                        }
                    }
            }

            // 2. Summary pipeline
            repository.getSummary(videoId).collect { result ->
                when (result) {
                    is AiResult.Loading -> {
                        _screenState.value = ScreenState.Loading(result.message)
                    }
                    is AiResult.Success -> {
                        _screenState.value = ScreenState.Summary(result)
                    }
                    else -> {
                        _screenState.value = ScreenState.Summary(result)
                    }
                }
            }

            // 3. Final cleanup: Auto-read toàn bộ text khi streaming hoàn tất
            val finalState = _screenState.value
            if (finalState is ScreenState.Summary && finalState.result is AiResult.Success) {
                // Trigger auto-read full text từ đầu
                _autoReadPending.value = true

                // Save to history (skip nếu từ cache)
                if (finalState.result.model != "cache") {
                    metadataJob.join()
                    repository.saveToHistory(
                        videoId = videoId,
                        title = _videoTitle.value,
                        thumbnailUrl = _thumbnailUrl.value,
                        summaryText = finalState.result.text
                    )
                }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        _screenState.value = screen
    }

    fun clearAutoRead() {
        _autoReadPending.value = false
    }

    fun setTtsPlaying(isPlaying: Boolean) {
        _isTtsPlaying.value = isPlaying
    }

    fun getTtsPausedIndex(): Int = ttsPausedIndex

    fun updateTtsPausedIndex(index: Int) {
        ttsPausedIndex = index
    }

    fun resetTtsPausedIndex() {
        ttsPausedIndex = 0
    }

    fun loadFromHistory(title: String, thumbnailUrl: String, summaryText: String) {
        _videoTitle.value = title
        _thumbnailUrl.value = thumbnailUrl
        _screenState.value = ScreenState.Summary(AiResult.Success(summaryText, "Local / History"))
    }

    fun getAllHistory() = repository.getAllHistory()
    
    suspend fun deleteHistoryItem(videoId: String) = repository.deleteHistoryItem(videoId)
    
    suspend fun clearAllHistory() = repository.clearAllHistory()

    private fun extractVideoId(url: String): String? {
        val pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        return Regex(pattern).find(url)?.groupValues?.get(1)
    }
}
