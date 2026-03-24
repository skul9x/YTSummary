# YTSummary

**YTSummary** là ứng dụng Android mạnh mẽ giúp trích xuất và tóm tắt phụ đề (transcript) từ bất kỳ video YouTube nào. Sử dụng **Jetpack Compose** cho giao diện người dùng mượt mà, kết hợp với công nghệ **Chaquopy** để chạy mã nguồn Python cục bộ (trích xuất phụ đề cực nhanh và tránh bị nền tảng lấy video chặn IP), cùng với mô hình AI **Gemini 2.5 Flash** cực kỳ thông minh đại diện bởi Google, giúp tóm tắt các đoạn phụ đề dài thành nội dung dễ hiểu và súc tích.

---

## 🌟 Tính Năng Nổi Bật

- **Trích Xuất Subtitle Cục Bộ**: Không còn lệ thuộc vào server trung gian! Script Python được triển khai cực kỳ gọn nhẹ ngay trên thiết bị Android bằng Chaquopy giúp lấy về transcript trực tiếp, phòng tránh tuyệt đối lỗi Data Center IP Blocking (như HTTP 502/403).
- **Tóm Tắt Bằng AI (Tư Duy Sâu)**: Tương tác với hệ thống AI Gemini 2.5 Flash thông qua mạng thuần OkHttp. Áp dụng luân chuyển API Key đa lớp (ModelQuota Caching) thông minh, tự động xoay tua để tránh tình trạng Rate Limit 429 hay Sever Busy 503.
- **Thao Tác Một Chạm (Paste & Summarize)**: Trải nghiệm người dùng tuyệt vời với việc quét bộ nhớ tạm tự động, bắt link YouTube và bắt đầu tóm tắt chỉ trong một lần chạm.
- **Lưu Trữ An Toàn Tuyệt Đối**: Chứa Room Database kết hợp định dạng mã hóa cao cấp SQLCipher, giúp lịch sử tìm kiếm và nội dung của bạn hoàn toàn bảo mật khỏi sự xâm nhập từ bên thứ ba.

---

## 📁 Cấu Trúc Thư Mục

```text
YTSummary/
├── app/                  # Source code ứng dụng Android chính
│   ├── build.gradle.kts  # Cấu hình Gradle (Chaquopy 17.0.0 & KSP Room)
│   └── src/main/
│       ├── java/com/skul9x/ytsummary/
│       │   ├── api/      # OkHttp Request kết nối Gemini API
│       │   ├── db/       # Room Database (SQLCipher)
│       │   ├── di/       # Dependency Injection (Network & Database Singleton Providers)
│       │   ├── manager/  # Hệ thống quản trị Caching Quota, API Keys và Chaquopy Router
│       │   ├── repository/# Hub dữ liệu gọi song song Metadata + Transcript
│       │   └── ui/       # Các Screen Jetpack Compose
│       └── python/       # Script xử lý trích xuất Transcript
├── .brain/               # Bộ nhớ của Hệ sinh thái AI (không sửa cấu trúc thư mục này)
└── docs/                 # Tài liệu Audit, Báo cáo và Specs gốc
```

---

## 🚀 Hướng Dẫn Cài Đặt

1. **Yêu cầu Môi Trường**: Bạn cần cài đặt Android Studio Meerkat (hoặc mới hơn), JDK 17+ và luôn đảm bảo kết nối mạng khi Build Gradle.
2. **Clone Dự Án**:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   cd YTSummary
   ```
3. **Cấu hình & Biên Dịch (Build)**:
   - Mở dự án trong Android Studio. 
   - Quá trình đồng bộ Gradle sẽ bắt đầu tự động tải **Chaquopy** (Python 3.12 Runtime cho thiết bị Android).
   - Nhấn **Run** ở toolbar hoặc dùng CLI: `./gradlew assembleDebug` để build ra bản APK.

---

## 💡 Cách Sử Dụng

1. Vào tab **Cài Đặt (Settings)** trong App. Dán trực tiếp các API Keys của nền tảng Gemini. Ứng dụng hỗ trợ "Dán Nhiều Mã (Multi-Paste)", nó đủ khôn khéo để tự bắt mọi Request Key theo Regex và lưu trữ cục bộ.
2. Trở ra màn chính, copy Link YouTube và bấm nút **Tóm tắt ngay**!
3. Khi app render nội dung hoàn chỉnh, hệ thống tự động lưu vào tab Data History để bạn thoải mái tra cứu lại mà không mất mạng.

---

## ©️ Bản Quyền

Copyright 2026 Nguyễn Duy Trường
