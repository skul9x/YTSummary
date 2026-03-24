# 🐛 Bug Report: TTS đọc từ giữa đoạn text khi Share Intent 1-Click

## Triệu chứng
Khi share link YouTube từ app khác (chế độ "Chia sẻ 1-Click"), sau khi tóm tắt xong và vào trang **AI Analysis**, TTS **không đọc từ đầu** mà đọc từ **giữa đoạn text**.

---

## Root Cause Analysis

### Luồng hoạt động khi Share Intent

```
1. handleIntent() → incomingUrl.value = url
2. LaunchedEffect(sharedUrl) → viewModel.summarize(url)
3. summarize() bắt đầu → emit Loading → emit Success (streaming SSE)
4. Mỗi lần emit AiResult.Success → _screenState = ScreenState.Summary(result)
5. Sentence detection → _ttsChunks.value = toRead  ← ĐÂY LÀ VẤN ĐỀ
6. LaunchedEffect(ttsChunk) → ttsManager.speakChunk(it) ← TỰ ĐỘNG ĐỌC
```

### Vấn đề cụ thể

**File:** `SummaryViewModel.kt` — hàm `summarize()`, dòng 79-91

```kotlin
is AiResult.Success -> {
    _screenState.value = ScreenState.Summary(result)

    // Sentence detection for TTS
    val newPart = result.text.substring(lastReadIndex)
    val lastPunct = newPart.lastIndexOfAny(
        charArrayOf('.', '!', '?', ':', '\n')
    )
    if (lastPunct != -1) {
        val toRead = newPart.substring(0, lastPunct + 1)
        _ttsChunks.value = toRead        // ← EMIT CHUNK TỰ ĐỘNG
        lastReadIndex += lastPunct + 1
    }
}
```

**File:** `MainActivity.kt` — TTS Chunk Observer, dòng 76-84

```kotlin
// TTS Chunk Observer
val ttsChunk by viewModel.ttsChunks.collectAsState()
LaunchedEffect(ttsChunk) {
    ttsChunk?.let {
        ttsManager.speakChunk(it)       // ← TỰ ĐỘNG ĐỌC NGAY LẬP TỨC
        viewModel.setTtsPlaying(true)
        viewModel.clearTtsChunk()
    }
}
```

### Diễn giải chi tiết

1. **Khi Gemini streaming SSE response**, mỗi chunk text đến → `_ttsChunks.value` được set → `LaunchedEffect` bắt TTS đọc **ngay lập tức** mà **KHÔNG cần user nhấn Play**.

2. **Vấn đề timing:** SSE streaming diễn ra rất nhanh (nhiều chunk/giây). `LaunchedEffect(ttsChunk)` dùng `StateFlow`, nên nếu nhiều chunk emit liên tiếp trước khi `LaunchedEffect` kịp chạy, **chỉ chunk cuối cùng được đọc** (StateFlow chỉ giữ giá trị mới nhất, không queue).

3. **Kết quả:** TTS bỏ qua các chunk đầu tiên (bị overwrite bởi chunk sau) → User nghe thấy TTS **bắt đầu đọc từ giữa** hoặc **từ cuối** đoạn text thay vì từ đầu.

4. Thêm vào đó, `lastReadIndex` vẫn tiếp tục tăng qua các chunk bị mất, nên phần "Final cleanup" ở dòng 99-105 cũng không bù lại được phần text đã bị skip.

### Tóm tắt nguyên nhân gốc

> **StateFlow chỉ giữ giá trị mới nhất (conflated).** Khi SSE streaming nhanh, nhiều chunk TTS bị ghi đè trước khi `LaunchedEffect` kịp consume → TTS bỏ qua các câu đầu → nghe như đọc từ giữa.

---

## Cách Fix Chi Tiết

### Phương án đề xuất: Tách riêng Auto-Read (streaming) vs Manual-Read (user click)

#### Ý tưởng core
- **Share Intent flow:** Không auto-read khi streaming. Đợi summary hoàn tất, rồi tự động đọc từ đầu 1 lần duy nhất bằng `ttsManager.speak(fullText, 0)`.
- **Manual flow (nhấn Play trên SummaryScreen):** Giữ nguyên logic hiện tại.

---

### Fix 1: SummaryViewModel.kt — Bỏ auto TTS chunk khi streaming

**Thay thế** block `is AiResult.Success` trong hàm `summarize()` (dòng 79-91):

```kotlin
// TRƯỚC (BUG):
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
```

```kotlin
// SAU (FIX):
is AiResult.Success -> {
    _screenState.value = ScreenState.Summary(result)
    // Không emit TTS chunk khi streaming nữa.
    // TTS sẽ được trigger sau khi streaming hoàn tất (xem Fix 2).
}
```

### Fix 2: SummaryViewModel.kt — Thêm auto-read sau khi streaming xong

Thêm flag `_autoReadPending` và emit tín hiệu đọc sau khi pipeline hoàn tất.

```kotlin
// Thêm state mới vào class SummaryViewModel:
private val _autoReadPending = MutableStateFlow(false)
val autoReadPending: StateFlow<Boolean> = _autoReadPending.asStateFlow()

fun clearAutoRead() {
    _autoReadPending.value = false
}
```

**Trong hàm `summarize()`**, block "Final cleanup" (dòng 99-117), thay thế:

```kotlin
// TRƯỚC:
// 3. Final cleanup: đọc phần text còn lại
val finalState = _screenState.value
if (finalState is ScreenState.Summary && finalState.result is AiResult.Success) {
    val remaining = finalState.result.text.substring(lastReadIndex)
    if (remaining.isNotBlank()) {
        _ttsChunks.value = remaining
    }
    // Save to history...
}
```

```kotlin
// SAU:
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
```

### Fix 3: MainActivity.kt — Observe autoReadPending thay vì ttsChunks

**Xóa** TTS Chunk Observer cũ (dòng 76-84):

```kotlin
// XÓA HOÀN TOÀN BLOCK NÀY:
// TTS Chunk Observer
val ttsChunk by viewModel.ttsChunks.collectAsState()
LaunchedEffect(ttsChunk) {
    ttsChunk?.let {
        ttsManager.speakChunk(it)
        viewModel.setTtsPlaying(true)
        viewModel.clearTtsChunk()
    }
}
```

**Thêm** Auto-Read Observer:

```kotlin
// Auto-Read Observer: đọc toàn bộ text từ đầu khi summary hoàn tất
val autoRead by viewModel.autoReadPending.collectAsState()
LaunchedEffect(autoRead) {
    if (autoRead) {
        val currentState = viewModel.screenState.value
        if (currentState is ScreenState.Summary) {
            val result = currentState.result
            if (result is AiResult.Success) {
                ttsManager.speak(result.text, 0)
                viewModel.setTtsPlaying(true)
            }
        }
        viewModel.clearAutoRead()
    }
}
```

### Fix 4: Cleanup — Xóa code không còn dùng

Trong `SummaryViewModel.kt`, có thể xóa hoặc giữ lại tùy ý:
- `_ttsChunks`, `ttsChunks`, `clearTtsChunk()` — không còn dùng cho streaming flow
- `lastReadIndex` — không còn cần thiết

---

## Kiểm tra sau khi fix

| Test Case | Expected |
|-----------|----------|
| Share link YouTube từ app khác | TTS đọc từ **đầu** đoạn summary, sau khi streaming hoàn tất |
| Nhấn Pause → Resume trên SummaryScreen | TTS resume đúng chỗ đã pause |
| Nhấn Restart | TTS đọc lại từ đầu |
| Mở từ History | TTS không tự động đọc (user phải nhấn Play) |
| Share link khi đang ở SummaryScreen cũ | TTS dừng cũ, đọc summary mới từ đầu |

---

## Tóm tắt 1 dòng

> **Bug:** `StateFlow` conflated → chunk TTS bị ghi đè khi SSE nhanh → TTS đọc từ giữa.  
> **Fix:** Bỏ auto-read từng chunk khi streaming. Đợi xong rồi `speak(fullText, 0)` một lần duy nhất.
