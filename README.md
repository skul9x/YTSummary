# YTSummary - YouTube AI Summarizer 🚀

**YTSummary** là ứng dụng Android mạnh mẽ giúp bạn tóm tắt nội dung video YouTube một cách nhanh chóng và thông minh bằng trí tuệ nhân tạo (Gemini AI). Tiết kiệm hàng giờ xem video dài bằng cách đọc bản tóm tắt súc tích ngay trên điện thoại của bạn.

## ✨ Tính năng nổi bật

- 📥 **Nhận Share Link**: Tóm tắt ngay lập tức bằng cách chia sẻ link từ ứng dụng YouTube.
- 🏎️ **Native Transcript Parser**: Lấy phụ đề video siêu tốc bằng Kotlin Native (không phụ thuộc backend).
- 🤖 **Gemini AI Integration**: Sử dụng các model Gemini mới nhất (Flash, Pro) để tóm tắt nội dung chính xác.
- 🔄 **Smart API Rotation**: Cơ chế xoay tua API Key và Model thông minh, tự động xử lý lỗi 429 (Rate Limit) và tối ưu hóa chi phí.
- 🎙️ **Smart TTS (Text-to-Speech)**: Đọc bản tóm tắt bằng giọng nói, hỗ trợ chia nhỏ văn bản dài và tự động dừng khi có cuộc gọi.
- 📱 **Giao diện Glassmorphism**: Thiết kế hiện đại, mượt mà với phong cách kính mờ (Kính mờ).
- 💾 **Lịch sử tóm tắt**: Lưu trữ và quản lý các video đã tóm tắt (Room Database).
- ⚡ **Hiệu năng tối ưu**: Khởi động cực nhanh nhờ Baseline Profiles và R8 Optimization.

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose (Modern Toolkit)
- **Kiến trúc**: MVVM (Model-View-ViewModel) + Clean Architecture
- **AI**: Google Gemini API (Vertex AI Client)
- **Database**: Room Persistence Library
- **Networking**: OkHttp3, Retrofit (cho các metadata API)
- **Image Loading**: Coil (với cơ chế caching tối ưu)
- **Concurrency**: Kotlin Coroutines & Flow
- **Dependency Injection**: Manual DI (Module-based)
- **Build System**: Gradle (Kotlin DSL)

## 📂 Cấu trúc thư mục chính

```text
app/src/main/java/com/skul9x/ytsummary/
├── api/            # Client và Helper cho Gemini & YouTube API
├── data/           # Room Database và Entities
├── di/             # Khởi tạo và quản lý dependencies
├── manager/        # Quản lý API Key, Quota, Model và TTS
├── model/          # Data classes và Sealed classes cho UI State
├── repository/     # Tầng trung gian xử lý logic dữ liệu
├── ui/             # Các màn hình (Compose) và ViewModels
└── util/           # Các tiện ích (Retry, Text Cleaning, Chunks)
```

## 🚀 Hướng dẫn cài đặt

1. **Clone repository**:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở dự án**: Sử dụng Android Studio (phiên bản Ladybug trở lên được khuyến nghị).
3. **Cấu hình API Key**:
   - Truy cập [Google AI Studio](https://aistudio.google.com/) để lấy Gemini API Key.
   - Mở ứng dụng, vào phần **Cài đặt** -> **Quản lý API Key** để thêm key của bạn.
4. **Build & Run**: Nhấn nút "Run" trong Android Studio để cài đặt lên thiết bị hoặc giả lập.

## 📝 Cách sử dụng

1. Mở app YouTube và tìm video bạn muốn tóm tắt.
2. Nhấn nút **Chia sẻ** (Share) -> Chọn **YTSummary**.
3. Ứng dụng sẽ tự động trích xuất phụ đề và bắt đầu tóm tắt.
4. Bạn có thể nghe bản tóm tắt bằng cách nhấn vào biểu tượng loa hoặc đọc văn bản trên màn hình.

## ⚙️ CI/CD

Dự án sử dụng **GitHub Actions** để tự động build file APK và tạo Release mỗi khi có thay đổi trên nhánh `main`. Bạn có thể tải bản build mới nhất trong phần [Releases](https://github.com/skul9x/YTSummary/releases).

## 📄 Bản quyền

Copyright 2026 Nguyễn Duy Trường

---
*Phát triển bởi Nguyễn Duy Trường với tình yêu dành cho Open Source.*
