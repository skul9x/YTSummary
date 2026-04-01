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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.collect
import java.util.concurrent.atomic.AtomicBoolean
import android.media.AudioManager


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
    
    private val _isTtsPlaying = MutableStateFlow(false)
    val isTtsPlaying: StateFlow<Boolean> = _isTtsPlaying.asStateFlow()

    private val _volumeLevel = MutableStateFlow(80)
    val volumeLevel: StateFlow<Int> = _volumeLevel.asStateFlow()

    private val _isVolumeActive = MutableStateFlow(false)
    val isVolumeActive: StateFlow<Boolean> = _isVolumeActive.asStateFlow()

    private val audioManager = application.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager

    // --- Optimized Streaming State ---
    val streamingChunks = mutableStateListOf<String>()
    private val isStreaming = AtomicBoolean(false)



    private var ttsPausedIndex = 0

    private var summaryJob: Job? = null

    init {
        // Pre-warm Database ở background để tránh lag khi query lần đầu
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.skul9x.ytsummary.data.AppDatabase.getDatabase(application)
        }
    }

    fun summarize(url: String) {
        val videoId = extractVideoId(url)
        if (videoId == null) {
            _screenState.value = ScreenState.Summary(AiResult.Error("URL hoặc Video ID không hợp lệ (cần đúng chuẩn YouTube 11 ký tự)."))
            return
        }
        
        // Cancel job cũ nếu đang chạy
        summaryJob?.cancel()
        resetTtsPausedIndex()
        setTtsPlaying(false)
        _screenState.value = ScreenState.Loading("📺 Đang lấy phụ đề...")

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
            streamingChunks.clear()
            isStreaming.set(true)
            
            repository.getSummary(videoId).collect { result ->
                when (result) {
                    is AiResult.Loading -> {
                        _screenState.value = ScreenState.Loading(result.message)
                    }
                    is AiResult.DeltaSuccess -> {
                        // Append current chunk to the list of chunks
                        // We use the last chunk and update it OR add a new one 
                        // to keep things simple for now we add a chunk.
                        // For even better performance, we could buffer them and add at a fixed cadence.
                        streamingChunks.add(result.chunk)
                        _screenState.value = ScreenState.Summary(result)
                    }
                    is AiResult.Success -> {
                        // Final state: summary complete, clear chunks and transition
                        isStreaming.set(false)
                        streamingChunks.clear()
                        _screenState.value = ScreenState.Summary(result)
                    }
                    else -> {
                        isStreaming.set(false)
                        streamingChunks.clear()
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

    fun toggleVolume() {
        if (!_isVolumeActive.value) {
            _isVolumeActive.value = true
        } else {
            val nextVolume = when (_volumeLevel.value) {
                80 -> 85
                85 -> 90
                else -> 80
            }
            _volumeLevel.value = nextVolume
        }

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (maxVolume * _volumeLevel.value / 100f).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
    }

    fun loadSummaryFromHistory(videoId: String) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading("Đang tải dữ liệu...")
            val summary = repository.getSummaryById(videoId)
            if (summary != null) {
                _videoTitle.value = summary.title
                _thumbnailUrl.value = summary.thumbnailUrl
                _screenState.value = ScreenState.Summary(AiResult.Success(summary.summaryText, "Local / History"))
            } else {
                _screenState.value = ScreenState.Summary(AiResult.Error("Không tìm thấy dữ liệu tóm tắt."))
            }
        }
    }

    // --- History Pagination ---
    val historyPagingData = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { repository.getHistoryItems() }
    ).flow.cachedIn(viewModelScope)
    
    fun getHistoryCount() = repository.getHistoryCount()

    suspend fun deleteHistoryItem(videoId: String) = repository.deleteHistoryItem(videoId)
    
    suspend fun clearAllHistory() = repository.clearAllHistory()

    private fun extractVideoId(url: String): String? {
        val trimmed = url.trim()
        
        // 1. Kiểm tra trực tiếp Video ID
        if (trimmed.matches(DIRECT_ID_REGEX)) {
            return trimmed
        }
        
        // 2. Trích xuất từ link YouTube
        return YOUTUBE_URL_REGEX.find(trimmed)?.groupValues?.get(1)
    }

    companion object {
        private val DIRECT_ID_REGEX = Regex("^[a-zA-Z0-9_-]{11}$")
        private val YOUTUBE_URL_REGEX = Regex("^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})")
    }
}
