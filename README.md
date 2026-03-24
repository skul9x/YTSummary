# YT Summary AI 📺🤖 — Smart Android YouTube Summarizer

**YT Summary AI** là một ứng dụng Android hiện đại, mạnh mẽ dành cho việc tóm tắt nội dung video YouTube bằng AI (Gemini 2.5 Flash). Dự án kết hợp hiệu quả giữa Kotlin (Jetpack Compose) và Python (chạy cục bộ qua Chaquopy), mang đến giải pháp tóm tắt nhanh, bảo mật và hoàn toàn ngoại tuyến trong khâu trích xuất nội dung.

---

## ✨ Tính năng nổi bật

- **Tóm tắt bằng Gemini 2.5 Flash**: Sử dụng model AI mới nhất của Google với khả năng tư duy (Thinking) để tạo ra các bản tóm tắt súc tích, chính xác.
- **Xử lý Python Cục bộ (Local Engine)**: Transcript được trích xuất trực tiếp trên thiết bị Android bằng Python (Chaquopy), không thông qua máy chủ trung gian, giúp bảo mật dữ liệu và khắc phục tình trạng bị chặn IP.
- **TTS Pause/Resume Thông minh**: Tích hợp Voice Assistant với khả năng đánh dấu vị trí đọc (Bookmark). Bạn có thể Tạm dừng và Tiếp tục đúng ngay tại từ đang nghe thay vì phải nghe lại từ đầu.
- **Chia sẻ 1-Click (Share Intent)**: Hỗ trợ chia sẻ trực tiếp từ ứng dụng YouTube chính thức sang YTSummary để tự động hóa hoàn toàn quy trình phân tích.
- **Quản lý Lịch sử (Room Database)**: Lưu trữ các bản tóm tắt vào cơ sở dữ liệu Room cục bộ, được mã hóa bằng SQLCipher để đảm bảo riêng tư tuyệt đối.
- **Thiết kế Glassmorphism Cao cấp**: Giao diện người dùng hiện đại với hiệu ứng "Kính mờ", bóng đổ Neon và các hiệu ứng micro-animations mượt mà.
- **Phục hồi Trạng thái (MVVM)**: Sử dụng ViewModel và StateFlow để bảo toàn tiến trình tóm tắt và vị trí TTS ngay cả khi xoay màn hình hoặc thay đổi cấu hình.

---

## 🛠️ Công nghệ sử dụng

- **Frontend**: Kotlin & Jetpack Compose (Declarative UI).
- **Trình xử lý lõi**: Python 3.11 thông qua Chaquopy 17.0.0.
- **AI Model**: Google Gemini API v1beta (Gemini 2.5 Flash).
- **Cơ sở dữ liệu**: Room + SQLCipher (Mã hóa cơ sở dữ liệu).
- **Networking**: OkHttp (Xử lý SSE Streaming cho AI output).
- **Kiến trúc**: MVVM + Repository Pattern + Clean Architecture principles.

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
   - Chạy ứng dụng trên máy ảo/thiết bị thật.
   - Vào phần **Settings**.
   - Dán API Key của bạn (Ứng dụng hỗ trợ Regex tự động lọc Key từ một đoạn văn bản dài).
5. **Build & Run**: Nhấn biểu tượng Play (Run) trong Android Studio.

---

## 📁 Cấu trúc thư mục dự án

```text
app/src/main/
├── java/com/skul9x/ytsummary/
│   ├── api/            # Hệ thống API client (Gemini SSE streaming)
│   ├── data/           # Database Room, SQLCipher & Dao
│   ├── manager/        # TtsManager, PythonManager (Bridge), ApiKeyManager
│   ├── model/          # Định nghĩa dữ liệu (AiResult, ScreenState)
│   ├── repository/     # Lớp quản lý dữ liệu & Coroutine guards
│   └── ui/             # Jetpack Compose Screens, Components & ViewModels
├── python/             # Logic trích xuất transcript bằng Python cục bộ
└── res/                # Tài nguyên UI & Adaptive Launcher Icons
.brain/                 # Trạng thái phiên làm việc & Kiến trúc dự án (Brain state)
```

---

## 📖 Cách sử dụng

1. **Dán Link**: Sao chép link YouTube và dán vào màn hình chính.
2. **Chia sẻ**: Trong ứng dụng YouTube, chọn **Chia sẻ** -> **YT Summary AI** để app tự động xử lý.
3. **Điều khiển TTS**:
   - **Play/Pause**: Tạm dừng hoặc tiếp tục đọc đúng vị trí cũ.
   - **Restart**: Bắt đầu nghe lại từ đầu.
4. **Lịch sử**: Xem lại các video đã tóm tắt trong mục **History**.

---

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được xây dựng với tư duy chất lượng cao và sự hỗ trợ của Antigravity AI Historian.*
