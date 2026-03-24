# TODO 2: Performance Optimization (Từ Fact-Check per_audit.txt)

> 7 vấn đề đã xác minh ĐÚNG 100% so với code thực tế. Dưới đây là hành động cụ thể cho từng cái.

---

## 1. ⬜ Chuyển `generateContent` → `streamGenerateContent` (SSE Streaming)
**File:** `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt` (Line 85)
**Vấn đề:** App gọi `generateContent` → phải chờ AI viết xong 100% mới hiển thị.
**Cách sửa:**
- Đổi URL từ `$model:generateContent` → `$model:streamGenerateContent?alt=sse`
- Parse response dạng Server-Sent Events (mỗi dòng bắt đầu bằng `data: {...}`)
- Trả về `Flow<String>` thay vì `AiResult.Success` đơn lẻ
- UI hiển thị chữ nhảy ra từng đoạn giống ChatGPT

---

## 2. ⬜ Pipeline đang chạy tuần tự → Cần tối ưu
**File:** `app/src/main/java/com/skul9x/ytsummary/repository/SummarizationRepository.kt` (Line 50-72)
**Vấn đề:** `getSummary()` chạy: Python (chờ) → Gemini (chờ) → trả kết quả. Không có song song.
**Cách sửa:**
- Nếu implement SSE (mục 1), pipeline tự nhiên cải thiện vì UI nhận data sớm hơn
- Có thể chạy song song `fetchMetadata()` + `fetchTranscript()` bằng `async/await` trong coroutine
- Kết hợp với Cache (mục 3) để short-circuit luồng xử lý

---

## 3. ⬜ Thêm Cache-First: Check DB trước khi gọi Python + Gemini
**File:** `app/src/main/java/com/skul9x/ytsummary/repository/SummarizationRepository.kt` (Line 50)
**File:** `app/src/main/java/com/skul9x/ytsummary/data/SummaryDao.kt`
**Vấn đề:** getSummary() KHÔNG kiểm tra DB → video đã tóm tắt rồi vẫn chạy lại toàn bộ pipeline 6 giây.
**Cách sửa:**
- **Bước 1:** Thêm hàm vào `SummaryDao.kt`:
  ```kotlin
  @Query("SELECT * FROM summaries WHERE videoId = :videoId LIMIT 1")
  suspend fun getSummaryById(videoId: String): SummaryEntity?
  ```
- **Bước 2:** Đầu hàm `getSummary()` trong Repository, check cache:
  ```kotlin
  val cached = summaryDao.getSummaryById(videoId)
  if (cached != null) {
      emit(AiResult.Success(cached.summaryText, "cache"))
      return@flow
  }
  ```
- **Hiệu quả:** Video xem lại → load từ DB chỉ ~10ms thay vì 6 giây.

---

## 4. ⬜ TTS đang đọc toàn bộ 1 lần → Cần chia nhỏ theo câu
**File:** `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt` (Line 89)
**Vấn đề:** Dùng `QUEUE_FLUSH` → đọc nguyên bài dài 1 lần. Phải chờ Gemini xong 100% mới bắt đầu đọc.
**Cách sửa:**
- Thêm hàm `speakChunk(text: String)` dùng `QUEUE_ADD` thay vì `QUEUE_FLUSH`
- Khi SSE stream trả về từng đoạn, detect dấu chấm câu (`.`, `?`, `!`) rồi gửi chunk cho TTS
- TTS bắt đầu đọc câu 1 trong khi Gemini vẫn đang viết câu 2, 3...
  ```kotlin
  fun speakChunk(textChunk: String) {
      if (!isInitialized) return
      val cleaned = cleanMarkdown(textChunk)
      if (cleaned.isEmpty()) return
      tts?.speak(cleaned, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
  }
  ```

---

## 5. ⬜ SSE Streaming là khả thi → Implement cho Gemini API
**File:** `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt`
**Xác nhận:** Gemini API hỗ trợ endpoint `streamGenerateContent?alt=sse` chính thức.
**Cách sửa:**
- Tạo hàm mới `summarizeStream(transcript: String): Flow<String>`
- Dùng OkHttp response body stream: `response.body?.source()` đọc từng dòng SSE
- Parse JSON từng chunk, extract `candidates[0].content.parts[0].text`
- Emit qua Kotlin Flow để UI và TTS nhận real-time

---

## 6. ✅ Regex đã pre-compile → KHÔNG CẦN LÀM GÌ
**File:** `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt` (Line 60-64)
**Trạng thái:** Đã tối ưu. Regex nằm trong `companion object` → chỉ compile 1 lần.
```kotlin
companion object {
    private val REGEX_MARKDOWN_SYMBOLS = Regex("[#*`~>_-]")
    // ... đã OK
}
```

---

## 7. ⬜ Kiến trúc Flow cần chuyển từ Batch → Stream
**Vấn đề:** Toàn bộ pipeline hiện tại là "Batch Processing" (chờ xong hết mới trả).
**Mục tiêu:** Chuyển sang "Stream Processing" để đạt Time-to-First-Byte < 1.5 giây.
**Flow mới:**
```
1. User paste URL → Check Cache DB (10ms)
2. Cache miss → Kotlin fetch transcript (thay Python, ~500ms)
3. Call Gemini streamGenerateContent
4. Chunk 1 arrives (~800ms) → UI render + TTS đọc câu 1
5. Chunk 2, 3... arrives → QUEUE_ADD vào TTS liên tục
```
**Phụ thuộc:** Cần hoàn thành mục 1, 3, 4, 5 trước.

---

## 📊 Thứ tự thực hiện đề xuất

| Bước | Task | Lý do |
|------|------|-------|
| 1 | Mục 3: Cache-First | Dễ nhất, hiệu quả cao nhất (~10ms vs 6s cho video lặp) |
| 2 | Mục 1+5: SSE Streaming | Cải thiện UX lớn nhất (chữ nhảy ra từng từ) |
| 3 | Mục 4: TTS Chunking | Kết hợp với SSE để đọc real-time |
| 4 | Mục 2+7: Pipeline redesign | Tổng hợp tất cả thành kiến trúc Stream |
| - | Mục 6: Regex | ✅ Đã xong, không cần động |
