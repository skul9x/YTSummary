# YT Summary AI 📺🤖 — Smart Android YouTube Summarizer

**YT Summary AI** là một ứng dụng Android hiện đại, mạnh mẽ dành cho việc tóm tắt nội dung video YouTube bằng AI (Gemini 2.5 Flash). Dự án kết hợp hiệu quả giữa Kotlin (Jetpack Compose) và Python (chạy cục bộ qua Chaquopy), mang đến giải pháp tóm tắt nhanh, bảo mật và hoàn toàn ngoại tuyến trong khâu trích xuất nội dung.

---

## 🚀 Trạng thái dự án: v4.2.0 (Stable & Secure)

Đây là phiên bản ổn định nhất hiện nay, được tối ưu hóa sâu về mặt bảo mật (HTTPS Enforcement), hiệu năng (StringBuilder buffering) và trải nghiệm người dùng (TTS Exact Bookmarking).

---

## ✨ Tính năng nổi bật

- **Tóm tắt bằng Gemini 2.5 Flash**: Sử dụng engine AI mạnh mẽ nhất của Google cho tác vụ tóm tắt với khả năng phân tích ngữ cảnh sâu.
- **Xử lý Python Cục bộ (Local Engine)**: Transcript được trích xuất trực tiếp trên thiết bị Android bằng Python (Chaquopy), vượt qua mọi rào cào chặn IP từ máy chủ trung gian.
- **Bảo mật đường truyền (Cleartext DISABLED)**: App được cấu hình chặn hoàn toàn kết nối HTTP không mã hóa, đảm bảo API Key luôn được truyền qua kênh HTTPS an toàn.
- **Validation Input nghiêm ngặt**: Hệ thống lọc Regex mạnh mẽ giúp nhận diện chính xác link/VideoID YouTube và lọc bỏ dữ liệu rác ngay tại ViewModel.
- **TTS Exact Bookmarking (Ghi nhớ vị trí đọc)**: Hệ thống Voice Assistant tự động lưu `totalSpokenLength` đến từng ký tự. Khi bấm "Tiếp tục", AI sẽ đọc đúng ngay vị trí đã dừng.
- **Python Runtime Background Warm-up**: Tự động sưởi ấm trình thông dịch Python trong nền, giảm thời gian khởi động app tới 2.5s.
- **Quản lý Lịch sử (Room + SQLCipher)**: Lưu trữ và mã hóa 256-bit AES cho toàn bộ cơ sở dữ liệu tóm tắt video.

---

## 🛠️ Công nghệ sử dụng

- **Frontend**: Kotlin & Jetpack Compose (Material Design 3).
- **Core Engine**: Python 3.11 tích hợp qua Chaquopy 17.0.0.
- **AI Integration**: Google Gemini API v1beta (Hỗ trợ SSE Streaming).
- **Serialization**: Kotlinx Serialization (Dùng cho cả Production và Stable Unit Tests).
- **Security**: EncryptedSharedPreferences (API Keys) & Network Security Policy.

---

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
- Thiết bị Android chạy hệ điều hành version 8.0 (API 26) trở lên.
- Có cài đặt Python 3.11 trên máy tính phát triển (để build APK với Chaquopy).

### Các bước phát triển
1. **Clone mã nguồn**:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở dự án**: Sử dụng **Android Studio Ladybug** (hoặc mới hơn).
3. **Cấu hình trên App**:
   - Truy cập **Settings**.
   - Dán nội dung chứa API Key (App hỗ trợ Regex tự nhận diện key `AIza...`).
4. **Build & Run**: Nhấn biểu tượng Play (Run) trong Android Studio hoặc chạy `./gradlew assembleDebug`.

---

## 📁 Cấu trúc thư mục dự án

```text
app/src/main/
├── java/com/skul9x/ytsummary/
│   ├── api/            # Gemini client & SSE streaming
│   ├── data/           # Database Room & SQLCipher
│   ├── manager/        # TtsManager (Bookmark), PythonUpdateChecker (Stable Test)
│   ├── repository/     # SummarizationRepository (Coroutine guards)
│   └── ui/             # Jetpack Compose UI & ViewModels (Shared Flow)
├── python/             # Transcript extraction scripts (Local IP)
└── res/                # Lottie Animations & Adaptive Icons
.brain/                 # Trạng thái phiên làm việc (Permanent context)
```

---

## 📖 Cách sử dụng

1. **Dán Link**: Copy YouTube URL và dán trực tiếp vào màn hình chính.
2. **Chia sẻ**: Trong YouTube app -> Share -> YT Summary AI (Auto-read ngay sau khi tóm tắt xong).
3. **Điều khiển TTS**:
   - **Play/Pause**: Tạm dừng và tiếp tục đọc từ chỗ cũ.
   - **Restart**: Bắt đầu nghe lại từ đầu.
4. **Lịch sử**: Xem lại các bản tóm tắt đã lưu trong quá khứ.

---

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được xây dựng với tư duy chất lượng cao và sự hỗ trợ của Antigravity AI Historian.*
