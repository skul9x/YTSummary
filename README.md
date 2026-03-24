# 📺 YTSummary

**YTSummary** là ứng dụng Android mạnh mẽ giúp bạn tóm tắt nội dung video YouTube nhanh chóng và hoàn toàn miễn phí. Dự án sử dụng mô hình LLM từ Google (Gemini API) kết hợp môi trường Python cục bộ (Chaquopy) để xử lý hoàn toàn trên thiết bị (Local Device), không phụ thuộc vào Backend trung gian.

---

## 🚀 Tính Năng Chính
- **Trích Xuất Phụ Đề Tốc Độ Cao:** Tích hợp trực tiếp thư viện Python `youtube-transcript-api` qua Chaquopy, xử lý việc tải sub cục bộ giúp vượt qua IP Block và lỗi 502 Cloud từ YouTube.
- **Tóm Tắt Nhanh & Súc Tích:** Được "trang bị vũ khí" với luồng AI từ Gemini 2.5 Flash, có khả năng xử lý ngữ cảnh cực dài và phân tích nhanh, giúp bạn nắm bắt ý chính video mà không mất thời gian xem.
- **Tính Năng Chuyển Đổi Văn Bản Thành Giọng Nói (TTS):** Hỗ trợ Text-to-Speech tự động đọc lên phần tóm tắt cho người thiết lập (rất hữu ích cho người đang lái xe hoặc làm việc khác).
- **Hỗ Trợ Đa API Key:** Nhập hàng loạt Gemini API key để xoay tua liên tục, giải quyết vấn đề Rate Limiting 429 và tăng tuổi thọ sử dụng, an toàn tuyệt đối với mã khóa cục bộ qua `EncryptedSharedPreferences`.
- **Lưu Trữ Bằng Lịch Sử DB:** SQLite Database thông qua tiện ích Room để lưu trữ offline toàn bộ các tóm tắt đã lưu. Tính năng mã hoá an toàn nhờ SQLCipher.

---

## 🛠️ Kiến Trúc Và Công Nghệ
- **Giao Diện UI:** Jetpack Compose (Modern toolkit), tích hợp hiệu ứng Glassmorphic / Neon.
- **Core Kotlin:** Coroutines, Flow, MVVM / Repository Pattern.
- **Network / API:** OkHttp.
- **Python Integration:** Chaquopy version 17.0.0 (Python 3.12).
- **Trí Tuệ Nhân Tạo (AI):** Dòng mô hình `models/gemini-2.5-flash` và các phiên bản liên quan.

---

## 📂 Cấu Trúc Thư Mục
Dự án được triển khai bằng thiết kế Modules:
```text
YTSummary/
├── app/
│   ├── src/main/java/com/skul9x/ytsummary/
│   │   ├── api/          # Xử lý Logic gửi request tới Gemini Google AI.
│   │   ├── data/         # Hệ thống CSDL Room Database.
│   │   ├── di/           # Network Module, Dependency injection Singleton.
│   │   ├── manager/      # Quản trị Quota, TTS, API key an toàn, Python (Chaquopy).
│   │   ├── model/        # Các Data Classes phục vụ UI và Business Logic.
│   │   ├── repository/   # Luồng Pipeline tổng để tổng kết video Fetch Transcript -> Generate AI.
│   │   └── ui/           # Các màn hình Compose: Home, Setup Settings, History, Summary Box.
│   └── src/main/python/  # Helper script Python extract Raw Youtube Transcript.
├── .brain/               # Bộ nhớ Context cho Antigravity (Không Xóa Thư Mục Này)
└── README.md
```

---

## 📲 Hướng Dẫn Cài Đặt Khởi Tạo
1. **Clone dự án về máy:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Khởi động IDE và Sync:** Mở Android Studio và chờ Gradle tải về các cấu hình Chaquopy cùng thư viện Jetpack.
3. **Biên Dịch App:** Chạy trên giả lập (Emulator) x86_64 hoặc build thành APK cài thẳng trên điện thoại qua lệnh Terminal `./gradlew assembleDebug`.
4. **Cấu Hình LLM:** Bạn sẽ cần lấy API Key từ Google AI Studio (miễn phí), copy và paste vô màn hình Cài Đặt trên ứng dụng điện thoại. (App không tự mang sẵn Key của Nhà Phát Triển nhằm bảo mật tài chính).

---

## 📄 Bản Quyền (License)
Dự án được xây dựng và duy trì bởi kỹ sư công nghệ phần mềm độc lập.

Copyright 2026 Nguyễn Duy Trường
