# System Overview - YTSummary
*Updated: 2026-03-25*

## 🏛️ Architecture: Standalone Mobile Client
Dự án đã được chuyển đổi hoàn toàn từ kiến trúc **Client-Server** (sử dụng FastAPI trên Railway) sang **Standalone Client**.

1. **Frontend (UI Layer):** 
   - Kotlin + Jetpack Compose
   - State management: `SummaryViewModel` (AndroidViewModel) + `StateFlow`
2. **Core Logic (Transcript Fetching):**
   - **Chaquopy 17.0.0** nhúng trực tiếp Python 3.12 vào trong file APK.
   - Thư viện `youtube-transcript-api` chạy bằng local IP của thiết bị di động, bypass toàn bộ giới hạn của YouTube Data Center IPs (502 Gateway, IP Blocking).
3. **AI Layer (Summarization):**
   - **Gemini API (v1beta)** gọi trực tiếp từ client.
   - Model mặc định: `models/gemini-2.5-flash` được cấu hình để có "Thinking" capability.
   - Tránh giới hạn API (Quota) bằng hệ thống **Key Rotation** (Lưu trữ an toàn tại `EncryptedSharedPreferences`).
4. **Performance Optimization (Audit v4.2.0):**
   - **Pipeline**: Stream Processing (SSE) -> StringBuilder Buffer -> Sentence Detect -> TSS speakChunk.
   - **Cold Start**: Python Warm-up (Background initialization).
   - **TTS Synchronization**: Đồng bộ hóa trạng thái engine với UI qua callback và bộ đếm hàng đợi (Atomic counter), khắc phục hiện tượng delay giữa các đoạn văn.
   - **Audio UX**: AudioFocus Ducking (Best practice).
5. **Data Layer (Storage):**
   - Database: **Room** + **SQLCipher** (Mã hóa toàn bộ file `.db`).
   - Cache-First Strategy: Check DB trước khi gọi Python/AI.
6. **Repository Pattern** kết nối Local Storage và Remote APIs.

## 🚀 Key Differences (Pre-Migration vs Post-Migration)
- Không còn REST API gọi qua Railway server.
- Không chịu phí backend server duy trì liên tục.
- Yêu cầu ứng dụng Android build to hơn (chứa môi trường Python x86_64, arm64-v8a). Tiết kiệm chi phí ở quy mô người dùng lớn.

## 📦 Tech Stack
| Tier | Technology |
|---|---|
| UI | Jetpack Compose, Material 3, Coil |
| Concurrency | Kotlin Coroutines, Flow |
| Local Scripting | Chaquopy 17.0, Python 3.12 |
| Security | SQLCipher, AndroidX Security Crypto |
| Network | OkHttp, Retrofit (deprecated use) |
| AI | Gemini API (Generative Language API) |
