## 📋 FIX PLAN (Actionable Steps)

---

### 🔧 Fix #1: Migrate State sang ViewModel (Priority: MEDIUM)

**Mục tiêu:** Đảm bảo pipeline summarization sống sót qua Configuration Change (xoay màn hình, đổi ngôn ngữ hệ thống). Tránh mất trạng thái đang xử lý.

#### Phase 1: Tạo `SummaryViewModel.kt`

**Tạo file mới:** `app/src/main/java/com/skul9x/ytsummary/ui/SummaryViewModel.kt`

```kotlin
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

    // Các câu TTS sẵn sàng đọc (producer-consumer qua Flow)
    private val _ttsChunks = MutableStateFlow<String?>(null)
    val ttsChunks: StateFlow<String?> = _ttsChunks.asStateFlow()

    private var summaryJob: Job? = null
    private var lastReadIndex = 0

    fun summarize(url: String) {
        val videoId = extractVideoId(url) ?: return
        
        // Cancel job cũ nếu đang chạy
        summaryJob?.cancel()
        lastReadIndex = 0
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

                        // Sentence detection for TTS
                        val newPart = result.text.substring(lastReadIndex)
                        val lastPunct = newPart.lastIndexOfAny(
                            charArrayOf('.', '!', '?', ':', '\n')
                        )
                        if (lastPunct != -1) {
                            val toRead = newPart.substring(0, lastPunct + 1)
                            _ttsChunks.value = toRead
                            lastReadIndex += lastPunct + 1
                        }
                    }
                    else -> {
                        _screenState.value = ScreenState.Summary(result)
                    }
                }
            }

            // 3. Final cleanup: đọc phần text còn lại
            val finalState = _screenState.value
            if (finalState is ScreenState.Summary && finalState.result is AiResult.Success) {
                val remaining = finalState.result.text.substring(lastReadIndex)
                if (remaining.isNotBlank()) {
                    _ttsChunks.value = remaining
                }

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

    fun clearTtsChunk() {
        _ttsChunks.value = null
    }

    fun getAllHistory() = repository.getAllHistory()
    
    suspend fun deleteHistoryItem(videoId: String) = repository.deleteHistoryItem(videoId)
    
    suspend fun clearAllHistory() = repository.clearAllHistory()

    private fun extractVideoId(url: String): String? {
        val pattern = "^(?:https?://)?(?:www\\.|m\\.)?(?:youtube\\.com/(?:(?:v|e(?:mbed)?)/|.*[?&]v=)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
        return Regex(pattern).find(url)?.groupValues?.get(1)
    }
}
```

#### Phase 2: Refactor `MainActivity.kt`

**1. Thêm dependency vào `app/build.gradle` (hoặc `app/build.gradle.kts`):**
```groovy
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"
// (Hoặc phiên bản mới nhất tương thích với project của bạn)
```

**2. Sửa file `MainActivity.kt`:**
Thay thế toàn bộ logic quản lý state bằng remember + scope thành sử dụng ViewModel. Xóa các logic liên quan đến `runBlocking`/`async` cũ và chỉ truyền/gọi ViewModel.

**Thay đổi chính:**
- Dùng `val viewModel: SummaryViewModel = viewModel()`
- Observe state: `val screenState by viewModel.screenState.collectAsState()`
- Quản lý TTS chunks thông qua `LaunchedEffect`:
```kotlin
val ttsChunk by viewModel.ttsChunks.collectAsState()
LaunchedEffect(ttsChunk) {
    ttsChunk?.let {
        ttsManager.speakChunk(it)
        viewModel.clearTtsChunk()
    }
}
```

---

### 🔧 Fix #2: Thêm Python Cancellation Guard (Priority: LOW)

**Mục tiêu:** Thêm kiểm tra `isActive` sau khi Python call trở về để tránh cập nhật luồng dữ liệu (emit/save) khi Coroutine (ViewModel scope) đã bị huỷ.

**Chỉnh sửa file:** `SummarizationRepository.kt`

**Thêm `ensureActive()` vào hàm `getSummary()`:**
```kotlin
// Import dòng này ở đầu file nếu chưa có
import kotlinx.coroutines.ensureActive

// Trong hàm getSummary(), cập nhật block lấy transcript:
fun getSummary(videoId: String): Flow<AiResult> = flow {
    // Check cache first ...

    emit(AiResult.Loading("📺 Đang lọc phụ đề qua Python..."))
    
    // Gọi Python
    val transcriptResult = pythonManager.fetchTranscript(videoId)

    // THÔNG MINH! Guard: Kiểm tra coroutine vẫn active sau khi blocking call Python trả về
    kotlinx.coroutines.currentCoroutineContext().ensureActive()

    if (transcriptResult.isFailure) {
        emit(AiResult.Error("Lỗi lấy phụ đề (Local): ${transcriptResult.exceptionOrNull()?.message}"))
        return@flow
    }

    // Các tác vụ lấy gemini đằng sau...
}.flowOn(kotlinx.coroutines.Dispatchers.IO) 
```

---

## 📌 Thứ tự thực thi

1. Cập nhật dependency lifecycle-viewmodel-compose trong Gradle.
2. Tạo file `SummaryViewModel.kt` theo code mẫu trên.
3. Refactor logic trong `MainActivity.kt` để thay thế cơ chế State cục bộ.
4. Thêm `ensureActive()` vào `SummarizationRepository.kt`.
5. Sync lại project và Verify bằng cách chạy app, nhấn lấy summary rồi từ từ xoay ngang màn hình để xem pipeline có giữ được không.
