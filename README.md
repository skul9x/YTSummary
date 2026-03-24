# YT Summary AI 📺🤖

**YouTube Summary AI** là một ứng dụng Android hiện đại giúp bạn tóm tắt nội dung video YouTube một cách nhanh chóng và thông minh bằng trí tuệ nhân tạo (Gemini AI). Ứng dụng kết hợp sức mạnh của Python (chạy cục bộ) và Kotlin Jetpack Compose để mang lại trải nghiệm mượt mà, bảo mật và hiệu quả.

---

## ✨ Tính năng nổi bật

- **Tóm tắt siêu tốc**: Sử dụng model Gemini 2.5 Flash mới nhất để đọc và phân tích video trong vài giây.
- **Xử lý Python Cục bộ**: Trích xuất phụ đề (transcript) trực tiếp trên thiết bị bằng Chaquopy, không thông qua server trung gian, giúp bảo vệ quyền riêng tư và tránh bị chặn IP.
- **Hệ thống TTS Pause/Resume thông minh**: Tự động đánh dấu vị trí đang đọc (Bookmark). Hỗ trợ Tạm dừng/Phát tiếp tục ngay tại từ đang đọc hoặc Đọc lại từ đầu.
- **Hỗ trợ đa ngôn ngữ**: Tóm tắt tốt cả video tiếng Việt và tiếng Anh (Hỗ trợ nhiều ngôn ngữ phụ đề).
- **Chia sẻ 1-Click**: Tích hợp Android Share Intent - Chia sẻ từ ứng dụng YouTube sang YTSummary để tự động hóa hoàn toàn quy trình.
- **Quản lý Lịch sử**: Lưu trữ các bản tóm tắt đã thực hiện trong cơ sở dữ liệu Room được mã hóa (SQLCipher).
- **Giao diện Modern Glassmorphism**: Thiết kế "Kính mờ" sang trọng với hiệu ứng bóng đổ Neon và Micro-animations tinh tế.
- **Kiến trúc MVVM**: Tiến trình tóm tắt và trạng thái TTS được bảo toàn khi xoay màn hình nhờ ViewModel và StateFlow.

---

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin (Frontend) & Python 3.11 (Local Engine).
- **UI Framework**: Jetpack Compose (Modern Declarative UI).
- **AI Engine**: Google Gemini API SDK (v1beta for thinking mode).
- **Python Bridge**: Chaquopy 17.0.0.
- **Database**: Room Database + SQLCipher (Encrypted per industry standard).
- **Kiến trúc**: MVVM + Repository Pattern + Clean Architecture logic.

---

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
- Android 8.0 (API 26) trở lên.
- Kết nối Internet (để gọi Gemini API).

### Các bước thực hiện (Dành cho nhà phát triển)
1. **Clone repository**:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở dự án**: Mở bằng **Android Studio Ladybug** (hoặc phiên bản mới hơn).
3. **Cài đặt Python**: Đảm bảo máy tính đã cài đặt Python 3.11 để tương thích với Chaquopy.
4. **Cấu hình API Key**:
   - Khởi chạy ứng dụng.
   - Truy cập **Settings**.
   - Dán một hoặc nhiều Gemini API Key (Ứng dụng hỗ trợ dán một đoạn văn bản dài lộn xộn, Regex sẽ tự lọc ra các Key bắt đầu bằng `AIza`).
5. **Build & Run**: Nhấn nút **Run** để cài đặt APK lên máy ảo hoặc thiết bị thật.

---

## 📖 Cách sử dụng

1. **Trực tiếp**: Dán URL video vào màn hình chính và nhấn nút **Go**.
2. **Chia sẻ**: Trong ứng dụng YouTube, nhấn **Chia sẻ** -> **YT Summary AI**. App sẽ tự động trích xuất metadata, transcript và thực hiện tóm tắt.
3. **TTS Điều khiển**:
   - Nút **Play/Pause**: Tạm dừng hoặc tiếp tục đọc đúng vị trí cũ.
   - Nút **Restart**: Đọc lại từ đầu bản tóm tắt.

---

## 📁 Cấu trúc thư mục

```text
app/src/main/
├── java/com/skul9x/ytsummary/
│   ├── api/            # Client kết nối Gemini (SSE Support)
│   ├── data/           # Room Database, SQLCipher & Dao
│   ├── manager/        # TtsManager, PythonManager, ApiKeyManager
│   ├── model/          # Data Models (AiResult, ScreenState)
│   ├── repository/     # Data Layer & Coroutine guards
│   └── ui/             # Compose Screens & ViewModels
├── python/             # Local Python Logic (yt-transcript-api integration)
└── res/                # UI Resources & Adaptive Icons
.brain/                 # AI Knowledge & Session State (Ghi nhớ ngữ cảnh phát triển)
```

---

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được xây dựng với tư duy Senior Dev và sự hỗ trợ của Antigravity AI Historian.*
