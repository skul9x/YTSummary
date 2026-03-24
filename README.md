# YT Summary AI 📺🤖 — Smart Android YouTube Summarizer

**YT Summary AI** là một ứng dụng Android hiện đại, mạnh mẽ dành cho việc tóm tắt nội dung video YouTube bằng AI (Gemini 2.5 Flash). Dự án kết hợp hiệu quả giữa Kotlin (Jetpack Compose) và Python (chạy cục bộ qua Chaquopy), mang đến giải pháp tóm tắt nhanh, bảo mật và hoàn toàn ngoại tuyến trong khâu trích xuất nội dung.

---

## 🚀 Trạng thái dự án: v4.1.0 (Production Ready)

Đây là phiên bản ổn định nhất hiện nay, được tối ưu hóa sâu về mặt hiệu năng (StringBuilder buffering) và trải nghiệm người dùng (TTS Exact Bookmarking).

---

## ✨ Tính năng nổi bật

- **Tóm tắt bằng Gemini 2.5 Flash**: Sử dụng engine AI mạnh mẽ nhất của Google cho tác vụ tóm tắt với khả năng phân tích ngữ cảnh sâu.
- **Xử lý Python Cục bộ (Local Engine)**: Transcript được trích xuất trực tiếp trên thiết bị Android bằng Python (Chaquopy), vượt qua mọi rào cào chặn IP từ máy chủ trung gian.
- **TTS Exact Bookmarking (Ghi nhớ vị trí đọc)**: Hệ thống Voice Assistant tự động lưu `totalSpokenLength` (tổng độ dài đã đọc) theo từng ký tự. Khi bấm "Tiếp tục" sau khi Pause, AI sẽ bắt đầu đọc đúng ngay tại vị trí đã dừng thay vì đọc lại toàn bộ đoạn văn.
- **Python Runtime Background Warm-up**: Tự động sưởi ấm (Warm-up) trình thông dịch Python ngay khi mở app trong nền, giúp giảm thời gian phân tích video lần đầu tới 2.5s.
- **Chia sẻ 1-Click (Share Intent)**: Hỗ trợ chia sẻ từ YouTube app sang YTSummary để tự động kích hoạt tiến trình tóm tắt và đọc tiếng (Auto-TTS) ngay khi hoàn tất.
- **Quản lý Lịch sử Bảo mật (Room + SQLCipher)**: Lưu trữ các bản tóm tắt vào cơ sở dữ liệu Room cục bộ, mã hóa 256-bit AES giúp bảo vệ dữ liệu cá nhân.

---

## 🛠️ Công nghệ sử dụng

- **Frontend**: Kotlin & Jetpack Compose (Declarative UI) với Material Design 3.
- **Core Engine**: Python 3.11 tích hợp sâu qua Chaquopy 17.0.0.
- **AI Integration**: Google Gemini API v1beta (Hỗ trợ SSE Streaming cho cảm giác chữ nhảy mượt mà).
- **Optimization**: StringBuilder buffer cho streaming giúp giảm độ trễ O(N²) khi nối chuỗi dài.
- **Security**: EncryptedSharedPreferences để lưu trữ API Keys an toàn trên thiết bị.

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
3. **Đăng ký Gemini API Key**: Lấy khóa API miễn phí từ [Google AI Studio](https://aistudio.google.com/).
4. **Cấu hình trên App**:
   - Chạy ứng dụng trên thiết bị.
   - Truy cập **Settings**.
   - Dán nội dung bất kỳ chứa API Key (App hỗ trợ Regex tự nhận diện key `AIza...`).
5. **Build & Run**: Nhấn biểu tượng Play (Run) trong Android Studio.

---

## 📁 Cấu trúc thư mục dự án

```text
app/src/main/
├── java/com/skul9x/ytsummary/
│   ├── api/            # Hệ thống API client (Tối ưu SSE streaming)
│   ├── data/           # Database Room, SQLCipher & Dao
│   ├── manager/        # TtsManager (Bookmark logic), PythonManager (Warm-up)
│   ├── model/          # Định nghĩa dữ liệu (AiResult, ScreenState)
│   ├── repository/     # Repository Pattern với Coroutine Safety guards
│   └── ui/             # Jetpack Compose UI & ViewModels (Shared Flow)
├── python/             # Transcript extraction scripts
└── res/                # Lottie Animations & Adaptive Icons
.brain/                 # Trạng thái phiên làm việc (Knowledge base)
```

---

## 📖 Cách sử dụng

1. **Dán Link**: Copy YouTube URL và dán trực tiếp vào Main Screen.
2. **Chia sẻ**: Trong YouTube app -> Share -> YT Summary AI.
3. **Điều khiển TTS**:
   - **Play/Pause**: Tạm dừng và tiếp sau đó sẽ đọc tiếp ngay từ chỗ cũ.
   - **Restart**: Reset vị trí đọc về đầu trang.
4. **Lịch sử**: Quản lý và xem lại lịch sử các video đã tóm tắt.

---

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được xây dựng với tư duy chất lượng cao và sự hỗ trợ của Antigravity AI Historian.*
