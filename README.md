# YT Summary AI 📺🤖

**YouTube Summary AI** là một ứng dụng Android hiện đại giúp bạn tóm tắt nội dung video YouTube một cách nhanh chóng và thông minh bằng trí tuệ nhân tạo (Gemini AI). Ứng dụng kết hợp sức mạnh của Python (chạy cục bộ) và Kotlin Jetpack Compose để mang lại trải nghiệm mượt mà, bảo mật và hiệu quả.

## ✨ Tính năng nổi bật

- **Tóm tắt siêu tốc**: Sử dụng model Gemini 2.5 Flash mới nhất để đọc và phân tích video trong vài giây.
- **Xử lý Python Cục bộ**: Trích xuất phụ đề (transcript) trực tiếp trên thiết bị bằng Chaquopy, không thông qua server trung gian, giúp bảo vệ quyền riêng tư và tránh bị chặn IP.
- **Hỗ trợ đa ngôn ngữ**: Tóm tắt tốt cả video tiếng Việt và tiếng Anh.
- **Text-to-Speech (TTS)**: Tự động đọc bản tóm tắt bằng giọng nói tự nhiên, hỗ trợ nghe rảnh tay.
- **Chia sẻ 1-Click**: Tích hợp Android Share Intent, chỉ cần nhấn "Chia sẻ" từ app YouTube sang YTSummary để bắt đầu tóm tắt ngay lập tức.
- **Quản lý Lịch sử**: Lưu trữ các bản tóm tắt đã thực hiện trong cơ sở dữ liệu Room được mã hóa (SQLCipher).
- **Giao diện Modern Glassmorphism**: Thiết kế kính mờ sang trọng, hỗ trợ chế độ tối (Dark Mode) và biểu tượng thích ứng (Adaptive Icon).
- **Bền bỉ (MVVM Architecture)**: Tiến trình tóm tắt không bị gián đoạn ngay cả khi xoay màn hình hoặc thay đổi cấu hình ứng dụng.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin (Frontend) & Python 3.11 (Backend on Android).
- **UI Framework**: Jetpack Compose.
- **AI Engine**: Google Gemini API (Vertex AI/Google AI SDK).
- **Python Bridge**: Chaquopy 17.0.0.
- **Database**: Room Database + SQLCipher (Mã hóa toàn bộ dữ liệu).
- **Kiến trúc**: MVVM (ViewModel, Flow, StateFlow), Repository Pattern.

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
- Android 8.0 (API 26) trở lên.
- Kết nối Internet.

### Các bước thực hiện (Dành cho nhà phát triển)
1. Clone repository:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. Mở dự án bằng **Android Studio Ladybug** (hoặc mới hơn).
3. Đảm bảo bạn đã cài đặt Python 3.11 trên máy tính để build Chaquopy.
4. Thêm API Key của bạn vào ứng dụng:
   - Mở ứng dụng, vào mục **Settings**.
   - Dán Gemini API Key của bạn vào (Ứng dụng sẽ tự động nhận diện Key bắt đầu bằng `AIza...`).
5. Build và chạy: Nhấn nút **Run** trong Android Studio.

## 📖 Cách sử dụng

1. **Cách 1 (Trực tiếp)**: Mở app, dán link YouTube vào ô nhập liệu và nhấn biểu tượng Play.
2. **Cách 2 (Chia sẻ)**: Khi đang xem video trên YouTube, nhấn nút **Chia sẻ** -> Chọn biểu tượng **YT Summary AI**. Ứng dụng sẽ tự động thực hiện mọi công đoạn còn lại.

## 📁 Cấu trúc thư mục chính

```text
app/src/main/
├── java/com/skul9x/ytsummary/
│   ├── api/            # Kết nối Gemini API
│   ├── data/           # Room Database & Entities
│   ├── manager/        # Quản lý Python, TTS, API Keys
│   ├── repository/     # Điều phối dữ liệu
│   └── ui/             # Jetpack Compose Screens & ViewModels
├── python/             # Mã nguồn Python chạy trên Android
│   └── yt_transcript_helper.py  # Logic lấy phụ đề video
└── res/                # Assets & Icons
```

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được phát triển bởi Nguyễn Duy Trường với sự hỗ trợ từ Antigravity AI.*
