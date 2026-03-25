# System Overview - YTSummary
*Updated: 2026-03-25*

## 🔄 Update Mechanism (Early Warning System)
Để duy trì tính ổn định của việc lấy transcript (do YouTube thường xuyên đổi cấu trúc), hệ thống tích hợp:
1. **PythonUpdateChecker**: Tự động gọi PyPI API (JSON endpoint) sau mỗi 24 giờ (có caching locally).
2. **MainScreen Notification**: Hiển thị Banner cảnh báo cập nhật trực tiếp tại màn hình chính nếu phiên bản `youtube-transcript-api` trong máy thấp hơn phiên bản trên PyPI.
3. **Manual Trigger**: Dev cập nhật phiên bản tại `app/build.gradle.kts` (khối `chaquopy.pip`) và thực hiện Gradle Sync.

## 🏛️ Architecture: Standalone Mobile Client
Dự án đã được chuyển đổi hoàn toàn từ kiến trúc **Client-Server** (sử dụng FastAPI trên Railway) sang **Standalone Client**.

1. **Frontend (UI Layer):** 
   - Kotlin + Jetpack Compose
   - State management: `SummaryViewModel` (AndroidViewModel) + `StateFlow`
2. **Core Logic (Transcript Fetching):**
   - **Chaquopy 17.0.0** nhúng trực tiếp Python 3.12 vào trong file APK.
   - Thư viện `youtube-transcript-api` chạy bằng local IP của thiết bị di động, bypass toàn bộ giới hạn của YouTube Data Center IPs (502 Gateway, IP Blocking).
3. **AI Layer (Summarization):**
   - **Gemini API (v1beta)** gọi trực tiếp từ client qua `GeminiApiClient`.
   - **Custom Model Management**: Người dùng tự định nghĩa danh sách model (Dynamic Model Priority) qua `ModelManager`.
   - **Key Rotation**: Tránh giới hạn API (Quota) bằng cơ chế xoay tua key trên từng model ưu tiên.
   - **Thinking Mode**: Đã vô hiệu hóa hoàn toàn bằng cách đặt `thinkingBudget = 0` ở root request để tối ưu tốc độ phản hồi.
4. **Performance Optimization (Audit v4.2.0):**
   - **Pipeline**: Stream Processing (SSE) -> StringBuilder Buffer -> Sentence Detect -> TSS speakChunk.
   - **Cold Start**: Python Warm-up (Background initialization).
   - **TTS Synchronization**: Đồng đồng bộ hóa trạng thái engine với UI qua callback và bộ đếm hàng đợi (Atomic counter).
   - **Audio UX**: AudioFocus Ducking (Best practice).
5. **Data Layer (Storage):**
   - Database: **Room** + **SQLCipher** (Mã hóa toàn bộ file `.db`).
   - Cache-First Strategy: Check DB trước khi gọi Python/AI.
6. **Repository Pattern** kết nối Local Storage và Remote APIs.
7. **Security Policy**:
   - `usesCleartextTraffic=false` enforces encrypted-only communication.
   - Strict Input Regex validates VideoID/URL before triggering processes.

## 🚀 Key Differences (Pre-Migration vs Post-Migration)
- Không còn REST API gọi qua Railway server.
- Không chịu phí backend server duy trì liên tục.
- Yêu cầu ứng dụng Android build to hơn. Tiết kiệm chi phí ở quy mô người dùng lớn.

## 📦 Tech Stack
| Tier | Technology |
|---|---|
| UI | Jetpack Compose, Material 3, Coil |
| Concurrency | Kotlin Coroutines, Flow |
| Local Scripting | Chaquopy 17.0, Python 3.12 |
| Security | SQLCipher, AndroidX Security Crypto, Network Security Config |
| Network | OkHttp, Retrofit (deprecated use) |
| AI | Gemini API (Generative Language API) |
| Serialization | Kotlinx Serialization (Standard for tests & production) |
| Validation | Regex Filter (Security & UX feedback) |
