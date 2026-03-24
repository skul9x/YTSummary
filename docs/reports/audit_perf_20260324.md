# 🏥 Performance Audit Report - YTSummary
### Date: 2026-03-24 | Auditor: Khang (Antigravity Code Doctor)
### Scope: Performance Focus | Architecture: Standalone (Local Python + Gemini API)

---

## 📊 Summary

| Severity | Count | Status |
|----------|-------|--------|
| 🔴 Critical (Ảnh hưởng trực tiếp đến tốc độ app) | 3 | Cần sửa ngay |
| 🟡 Warning (Gây lãng phí tài nguyên) | 4 | Nên sửa |
| 🟢 Suggestion (Tối ưu thêm) | 4 | Tùy chọn |

**Tổng điểm sức khỏe: 6.5/10** 🩺
> App hoạt động được nhưng có nhiều chỗ "tắc nghẽn mạch máu" gây chậm và hao pin. Cần điều trị sớm.

---

## 🔴 CRITICAL ISSUES (Phải sửa ngay!)

### 🔴 C1. SummarizationRepository tạo mới objects mỗi lần khởi tạo
- **File:** `repository/SummarizationRepository.kt` (Line 13-21)
- **Triệu chứng:** Mỗi khi `SummarizationRepository(this)` được gọi, nó tạo MỚI:
  - `AppDatabase.getDatabase()` (tuy có singleton nhưng vẫn gọi synchronized check)
  - `GeminiApiClient(...)` (tạo mới hoàn toàn mỗi lần)
  - `ModelQuotaManager(context)` (tạo mới, load lại SharedPreferences, parse JSON)
- **Hậu quả:** Nếu user navigate qua lại nhiều lần hoặc Activity recreate (xoay màn hình), tất cả objects bị tạo lại → **tốn RAM, CPU, và I/O đọc disk** không cần thiết.
- **Mức độ nguy hiểm:** ⭐⭐⭐⭐ (Cao - Gây lag khi xoay màn hình hoặc config change)
- **Cách sửa:**
  ```kotlin
  // SummarizationRepository nên là Singleton hoặc inject qua ViewModel
  // ModelQuotaManager cũng nên là Singleton giống ApiKeyManager và PythonManager
  ```

### 🔴 C2. Metadata và Summary fetch chạy TUẦN TỰ thay vì song song
- **File:** `ui/MainActivity.kt` (Line 62-76)
- **Triệu chứng:** Code hiện tại:
  ```kotlin
  // Step 1: Fetch Metadata (chờ xong rồi mới...)
  repository.getVideoMetadata(videoId).collect { ... }
  // Step 2: Fetch Summary (mới bắt đầu)
  repository.getSummary(videoId, ...).collect { ... }
  ```
  Hai bước này chạy **nối tiếp nhau**. Metadata (oEmbed) mất ~500ms, Transcript (Python) mất ~2-5s. Tổng cộng user phải chờ **cộng dồn** thời gian cả hai.
- **Hậu quả:** User chờ lâu hơn 500ms-1s so với cần thiết. Trải nghiệm kém.
- **Mức độ nguy hiểm:** ⭐⭐⭐⭐⭐ (Rất cao - Ảnh hưởng trực tiếp UX)
- **Cách sửa:**
  ```kotlin
  scope.launch {
      // Chạy song song cả hai
      val metadataJob = async { repository.getVideoMetadata(videoId).first() }
      val summaryJob = async { repository.getSummary(videoId, ...).first() }
      
      val metadata = metadataJob.await()
      // Update UI với metadata ngay khi có
      val summary = summaryJob.await()
      // Navigate to summary screen
  }
  ```

### 🔴 C3. PythonManager.fetchTranscript() chạy trên Main Thread (Blocking I/O)
- **File:** `manager/PythonManager.kt` (Line 30-53)
- **Triệu chứng:** `fetchTranscript()` và `fetchMetadata()` là **blocking functions** (không có `suspend`). Chúng gọi Python interpreter đồng bộ, mất 2-5 giây.
- **Hậu quả:** Mặc dù `SummarizationRepository` dùng `flow {}` (chạy trên coroutine), nhưng **flow builder mặc định chạy trên context của collector** (Main thread). Nếu không specify `Dispatchers.IO`, Python call sẽ **BLOCK UI thread** → App đơ, ANR (Application Not Responding).
- **Mức độ nguy hiểm:** ⭐⭐⭐⭐⭐ (Rất cao - Có thể gây crash ANR)
- **Cách sửa:**
  ```kotlin
  // Trong SummarizationRepository, thêm flowOn(Dispatchers.IO):
  fun getSummary(...): Flow<AiResult> = flow {
      // ... existing code ...
  }.flowOn(Dispatchers.IO)
  
  fun getVideoMetadata(...): Flow<VideoMetadata?> = flow {
      // ... existing code ...
  }.flowOn(Dispatchers.IO)
  ```

---

## 🟡 WARNINGS (Nên sửa)

### 🟡 W1. Model list chứa model không tồn tại → Request thất bại tốn thời gian
- **File:** `api/GeminiApiClient.kt` (Line 127-133)
- **Triệu chứng:** Danh sách `MODELS` chứa:
  - `models/gemini-2.5-flash-lite` (chưa chắc tồn tại)
  - `models/gemini-3-flash-preview` (chưa phát hành)
  Mỗi model không tồn tại sẽ trả về HTTP 404, tốn **1-3 giây cho mỗi lần thử** × số API keys.
- **Hậu quả:** Nếu có 3 keys và 2 model ảo → 6 request thất bại = **6-18 giây lãng phí**.
- **Cách sửa:** Chỉ giữ lại các model đã verify hoạt động. Thêm caching cho model availability.

### 🟡 W2. ModelQuotaManager tính SHA-256 hash mỗi lần check availability
- **File:** `manager/ModelQuotaManager.kt` (Line 61-71)
- **Triệu chứng:** `makeKey()` gọi `MessageDigest.getInstance("SHA-256")` và tính hash **mỗi lần** `isAvailable()` được gọi. Trong vòng lặp rotation (5 models × N keys), hàm này chạy rất nhiều lần.
- **Hậu quả:** CPU overhead không cần thiết trên mỗi request. Trên thiết bị yếu có thể gây jank.
- **Cách sửa:** Cache kết quả hash của API key.

### 🟡 W3. Legacy code và dependencies vẫn còn sót lại
- **Files:**
  - `api/YouTubeApiClient.kt` → Interface Retrofit cho backend cũ (Railway), **KHÔNG CÒN DÙNG**
  - `utils/Constants.kt` → `BASE_URL` trỏ tới Railway, **KHÔNG CÒN DÙNG**
  - `di/NetworkModule.kt` → Retrofit instance cho backend cũ, **CHỈ DÙNG OkHttpClient**
  - `model/TranscriptResponse.kt` → Data class cho API cũ
  - `build.gradle.kts` Line 23 → `buildConfigField("String", "BASE_URL", ...)` trỏ Railway
- **Triệu chứng:** Dead code nằm trong APK, tăng kích thước app. Retrofit dependency (~300KB) được bundle nhưng chức năng chính không dùng.
- **Hậu quả:** APK lớn hơn cần thiết (~500KB-1MB dead code + dependencies). ProGuard có thể loại bớt nhưng không hoàn toàn.
- **Cách sửa:** Xóa các file legacy, loại bỏ Retrofit dependencies nếu không cần.

### 🟡 W4. TtsManager gọi setVolume(80) MỖI LẦN speak()
- **File:** `manager/TtsManager.kt` (Line 81)
- **Triệu chứng:** Method `speak()` luôn gọi `setVolume(80)` trước khi phát. Mỗi lần gọi `setVolume()` sẽ truy cập AudioManager system service.
- **Hậu quả:** I/O overhead nhỏ nhưng không cần thiết vì volume đã được set ở `onCreate`.
- **Cách sửa:** Chỉ set volume lần đầu (đã có trong `onCreate`), bỏ call thừa trong `speak()`.

---

## 🟢 SUGGESTIONS (Tùy chọn - Nâng cấp)

### 🟢 S1. MainScreen stat cards hiển thị số cứng ("32 Summaries")
- **File:** `ui/MainActivity.kt` (Line 273)
- **Triệu chứng:** Text "32" là hardcoded, không phản ánh số lượng summary thực tế từ database.
- **Gợi ý:** Query `summaryDao.count()` và hiển thị số thật.

### 🟢 S2. Thumbnail image không cache giữa các màn hình
- **File:** `ui/SummaryScreen.kt`, `ui/HistoryScreen.kt`
- **Triệu chứng:** Coil mặc định có memory cache, nhưng không có disk cache config rõ ràng.
- **Gợi ý:** Configure Coil `ImageLoader` với disk cache để tránh re-download thumbnails.

### 🟢 S3. Regex extractVideoId() được compile mỗi lần gọi
- **File:** `ui/MainActivity.kt` (Line 142-144)
- **Triệu chứng:** `Regex(pattern)` tạo mới object mỗi lần user nhấn nút.
- **Gợi ý:** Di chuyển regex vào companion object hoặc lazy val.

### 🟢 S4. GeminiResponseHelper.extractText() parse full JSON mỗi lần
- **File:** `api/gemini/GeminiResponseHelper.kt` (Line 45-72)
- **Triệu chứng:** Dùng `Json.parseToJsonElement()` parse toàn bộ response body (có thể lớn). Không có streaming parser.
- **Gợi ý:** Với response nhỏ (< 100KB) thì OK. Nếu transcript dài, cân nhắc dùng streaming JSON parser hoặc giới hạn response size.

---

## 🏗️ Architecture Performance Notes

### ✅ Điểm tốt đã làm đúng:
1. **PythonManager dùng Singleton pattern** → Không khởi tạo Python runtime nhiều lần
2. **ApiKeyManager dùng Singleton pattern** → Tốt
3. **GeminiApiClient pre-builds prompt TRƯỚC vòng lặp rotation** → Tiết kiệm CPU
4. **OkHttp dùng lazy singleton** → Connection pool được tái sử dụng
5. **HistoryScreen dùng LazyColumn với key** → Efficient list rendering
6. **Room DAO trả về Flow** → Reactive UI updates
7. **suspendCancellableCoroutine cho OkHttp** → Proper coroutine integration
8. **R8/ProGuard enabled cho release build** → Code shrinking

### ⚠️ Kiến trúc cần cải thiện:
1. **Không có ViewModel** → State management nằm trong Activity, dễ mất state khi config change
2. **Không có DI framework** → Objects tạo thủ công, khó test và gây duplicate instances
3. **Navigation bằng String state** → Không dùng Navigation Compose, mất type-safety

---

## 📋 Bảng tóm tắt ưu tiên sửa

| # | Issue | Impact | Effort | Priority |
|---|-------|--------|--------|----------|
| C3 | Flow không chạy trên IO dispatcher | ANR/Crash | 5 phút | 🔴 P0 |
| C2 | Metadata + Summary chạy tuần tự | Chậm 500ms-1s | 15 phút | 🔴 P0 |
| C1 | Repository/QuotaManager tạo mới mỗi lần | RAM leak, lag | 30 phút | 🔴 P1 |
| W1 | Model list có model ảo | Chậm 6-18s | 5 phút | 🟡 P1 |
| W3 | Legacy dead code trong APK | APK size +1MB | 20 phút | 🟡 P2 |
| W4 | setVolume() gọi thừa | Minor I/O | 2 phút | 🟡 P3 |
| W2 | SHA-256 tính lại mỗi lần | CPU overhead | 10 phút | 🟡 P3 |

---

## ⏭️ Next Steps

```
📋 Anh muốn làm gì tiếp theo?

1️⃣ Xem lại chi tiết từng issue
2️⃣ Sửa lỗi Critical ngay (dùng /code) 
3️⃣ Dọn dẹp legacy dead code (dùng /refactor)
4️⃣ Bỏ qua, lưu báo cáo vào /save-brain
5️⃣ 🔧 FIX ALL - Tự động sửa TẤT CẢ lỗi có thể sửa

Gõ số (1-5) để chọn:
```
