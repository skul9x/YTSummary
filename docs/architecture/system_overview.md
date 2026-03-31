# System Overview - YTSummary
*Last Updated: 2026-03-31*

## 🏛️ Architecture: Pure Native Mobile Client
Dự án đã được chuyển đổi hoàn toàn sang kiến trúc **Native Standalone Client**, loại bỏ 100% phụ thuộc vào Python (Chaquopy) và Backend Server (Railway).

1. **Frontend (UI Layer):** 
   - Kotlin + Jetpack Compose (MVVM Architecture)
   - State management: `SummaryViewModel` + `UiState` (Gom nhóm StateFlow để tối ưu Recomposition).
   - Rendering: Sử dụng `LazyColumn` với cơ chế **Text Chunking** để hiển thị các bản tóm tắt cực dài mà không lag.

2. **Core Logic (Native Transcript Fetching):**
   - **YouTubeTranscriptService**: Xây dựng hoàn toàn bằng Kotlin, gọi trực tiếp tới các endpoint nội bộ của YouTube Captions (timedtext).
   - **TranscriptParser**: Parser XML tốc độ cao, tối ưu CPU bằng cách sử dụng unescape thủ công thay vì Android `Html.fromHtml`.
   - **InnerTube API**: Trích xuất Metadata video (thumbnail, title, author) trực tiếp từ client.

3. **AI Layer (Summarization):**
   - **Google Gemini API (v1beta)**: Giao tiếp trực tiếp qua `GeminiApiClient`.
   - **SSE Streaming**: Hiển thị kết quả tóm tắt ngay khi AI vừa bắt đầu phân tích.
   - **Key Rotation**: Cơ chế xoay tua khóa API (Model-First Key Rotation) để vượt qua giới hạn Quota.

4. **Performance & Reliability (Audit v5.0.0):**
   - **Non-blocking Retries**: Cơ chế retry sử dụng `delay` của Coroutines thay vì chặn Thread (blocking sleep).
   - **Network Timeout Separation**: 
     - 15s cho các request Metadata/Connect (fail fast).
     - 90s cho Gemini Stream (đảm bảo tóm tắt video dài ổn định).
   - **R8/Minify**: Bật tối ưu hóa mã nguồn và nén tài nguyên trong bản Release.
   - **Baseline Profiles**: Cải thiện thời gian khởi chạy ứng dụng (AOT compilation).

5. **Data Layer (Storage):**
   - Database: **Room** + **SQLCipher** (Mã hóa toàn bộ DB).
   - Paging 3: Load danh sách lịch sử vô hạn với bộ nhớ tối thiểu.

6. **Security Policy:**
   - Enforce HTTPS via `NetworkSecurityConfig`.
   - Local sensitive data storage (API Keys) được mã hóa.

## 🚀 Key Improvements (Post-Performance Optimization)
- **Startup Time**: Cải thiện ~40% do gỡ bỏ Python Runtime (giảm APK size và warm-up time).
- **Memory Footprint**: Giảm memory churn khi scroll tóm tắt dài nhờ Lazy Loading + Chunking.
- **Network Resilience**: Xử lý lỗi mạng mượt mà hơn với Async Retry Interceptor.

## 📦 Tech Stack
| Tier | Technology |
|---|---|
| UI | Jetpack Compose, Material 3, Coil |
| Concurrency | Kotlin Coroutines, StateFlow |
| Transcript | Native Kotlin (YouTube TimedText API) |
| Security | SQLCipher, AndroidX Security Crypto |
| Network | OkHttp3 (Separated Timeouts, Retry Interceptor) |
| AI | Gemini API (Streaming mode) |
| Profiling | Macrobenchmark module |

---

## 🏎️ Network & Optimization Matrix

| Feature | Scope | Logic | Impact |
|---|---|---|---|
| **Lazy Summary** | UI | `LazyColumn` + Piecewise Rendering | Cuộn mượt mà bản tóm tắt > 10,000 từ |
| **Separated Timeout** | Network | 15s Connect / 90s Stream | Tránh kẹt socket nhưng không làm hỏng stream |
| **Native Parser** | Processing | Manual String Unescape | Giảm tải CPU khi xử lý hàng ngàn dòng XML |
| **Async Retries** | Network | Non-blocking Exponential Backoff | UI không bị "đóng băng" khi mất mạng tạm thời |
