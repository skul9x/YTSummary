# YTSummary - Trợ Lý Tóm Tắt Video YouTube Bằng AI 🚀

**YTSummary** là một ứng dụng Android hiện đại giúp người dùng tiết kiệm thời gian bằng cách tự động trích xuất nội dung và tóm tắt các video YouTube dài thành những ý chính ngắn gọn, súc tích bằng công nghệ AI của Google (Gemini).

## 🌟 Tính năng nổi bật

- **Tóm tắt siêu tốc**: Sử dụng model **Gemini 2.5 Flash** mới nhất với độ trễ tối thiểu (Thinking mode disabled).
- **Trích xuất transcript nội bộ**: Tự động lấy phụ đề video trực tiếp trên thiết bị (Local) thông qua Chaquopy, không thông qua server trung gian, giúp bảo mật và ổn định.
- **Hỗ trợ đa ngôn ngữ**: Tóm tắt các video tiếng Anh/Việt sang nội dung tiếng Việt rõ ràng.
- **Đọc nội dung (TTS)**: Tích hợp Text-to-Speech với khả năng Tạm dừng/Tiếp tục thông minh (Smart Bookmarking).
- **Chia sẻ 1-click**: Hỗ trợ nhận link trực tiếp qua Android Share Intent từ ứng dụng YouTube.
- **Bảo mật & Mã hóa**: Lưu trữ lịch sử an toàn với database **SQLCipher** (Mã hóa toàn bộ dữ liệu).
- **Thiết kế Glassmorphism**: Giao diện trẻ trung, mượt mà với hiệu ứng kính mờ và Adaptive Icon.

## 🛠️ Yêu cầu hệ thống

- **Android SDK**: tối thiểu API 24 (Android 7.0).
- **Gemini API Key**: Bạn cần có API key từ [Google AI Studio](https://aistudio.google.com/).
- **Ngôn ngữ lập trình**: 100% Kotlin (Jetpack Compose).
- **Backend nội bộ**: Python 3.12 tích hợp qua Chaquopy.

## 📦 Hướng dẫn cài đặt

1. **Clone repository**:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Cấu hình Gemini API**:
   - Mở ứng dụng sau khi cài đặt.
   - Truy cập vào màn hình **Cài đặt (Settings)**.
   - Dán nội dung chứa API Key (App hỗ trợ Regex tự nhận diện key `AIza...`).
3. **Build & Chạy**:
   - Mở dự án trong Android Studio (Ladybug or later).
   - Đảm bảo bạn đã cài đặt Python 3.11+ trên máy tính để Chaquopy build thành công.
   - Nhấn **Run** để cài đặt lên thiết bị.

## 📖 Cách sử dụng

1. **Cách 1 (Thủ công)**: Copy link YouTube, dán vào ô nhập liệu ở màn hình chính và nhấn nút "Tóm tắt".
2. **Cách 2 (Chia sẻ)**: Khi đang xem video trên YouTube, nhấn nút **Chia sẻ** -> Chọn ứng dụng **YTSummary**. Ứng dụng sẽ tự động trích xuất và tóm tắt ngay lập tức.
3. **Đọc tóm tắt**: Sau khi tóm tắt xong, bạn có thể nhấn biểu tượng Loa để AI đọc nội dung cho bạn nghe.

## 📂 Cấu trúc thư mục chính

```text
app/src/main/java/com/skul9x/ytsummary/
├── api/             # Client gọi Gemini API (SSE Streaming)
├── manager/         # Quản lý Python Runtime, API Keys, TTS
├── model/           # Data classes (AiResult, ScreenState)
├── repository/      # Xử lý Logic Fetch & Database (Room + SQLCipher)
├── ui/              # Giao diện Jetpack Compose (SummaryScreen, History...)
└── utils/           # Helper functions (Constants, Regex...)
```

## 🔒 Bản quyền & Giấy phép

Copyright 2026 Nguyễn Duy Trường.
Tất cả các quyền được bảo lưu.

---
*Dự án được xây dựng và tối ưu hoàn toàn bởi Antigravity AI.*
