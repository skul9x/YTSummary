# YTSummary AI 🚀

YTSummary là một ứng dụng di động trên nền tảng Android, giúp người dùng bóc tách phụ đề (transcript) của video YouTube và tự động tóm tắt nội dung một cách trực quan, đầy đủ. 
Dự án được xây dựng với kiến trúc Standalone bảo mật, tích hợp trực tiếp môi trường Python cục bộ vào ứng dụng Android, từ đó tránh bị báo lỗi, block IP của YouTube nhờ chạy bằng IP nội bộ máy cá nhân, sau đó kết hợp sức mạnh siêu việt của mô hình AI Gemini (Gemini 2.5 Flash) để tạo ra các bài tóm tắt chất lượng.

![YTSummary Hero](https://i.imgur.com/example-hero.png) <!-- Replace with real image -->

## 🌟 Chức năng nổi bật
*   **Trích xuất Subtitle Cục bộ (Local Fetch):** Tích hợp Youtube-Transcript-API bằng Chaquopy trực tiếp vào thiết bị Android. (Bypass triệt để tình trạng 502/IP Block thường gặp khi fetch trên mây Cloud).
*   **Tóm tắt thông minh:** Sử dụng Google Gemini 2.5 Flash có tính năng Thinking tích hợp logic suy luận sâu.
*   **Lưu trữ đám mây:** Cơ sở dữ liệu Room Database mã hóa (SQLCipher) an toàn, nhanh gọn, không chia sẻ lên mây riêng tư.
*   **Quản lý Khóa API:** Tính năng tự động quét từ clipboard và xoay tua tự động giữa nhiều khóa API khác nhau tránh việc cạn Limit (Quota).
*   **Audio Đọc văn bản (AI TTS):** Trợ lý ảo đọc nội dung tóm tắt bằng giọng nói rõ ràng.

## 🛠️ Công nghệ sử dụng
*   **Frontend:** Kotlin, Jetpack Compose, Material Design 3.
*   **Xử lý Ngoại Vi (Core Runtime):** Chaquopy 17.0.0 (Python 3.12 embedded trong Android).
*   **Lưu trữ Dữ Liệu:** Room Database (Offline), Encrypted SQLite.
*   **Mạng & AI:** Retrofit, OkHttp, Google Gemini API endpoints.

## 🎯 Cấu trúc Thư mục

```text
YTSummary/
├── app/
│   ├── src/main/java/com/skul9x/ytsummary/
│   │   ├── api/          # Kết nối đến Gemini AI
│   │   ├── data/         # CSDL Room, Entity
│   │   ├── manager/      # Quản lý khóa API, Quota, TTS
│   │   ├── model/        # Các Data classes, models 
│   │   ├── repository/   # Repository Pattern xử lý Data
│   │   └── ui/           # Các màn hình Jetpack Compose (Home, Analysis, History...)
│   └── src/main/python/
│       └── yt_transcript_helper.py # Môi trường Python nội bộ (Chaquopy)
├── .brain/               # Bộ nhớ của AI Antigravity tự động generate hỗ trợ code
├── plans/                # Project Workflow Plans của dự án
└── build.gradle.kts      # Cấu hình Gradle và cài đặt thư viện pip (Transitive)
```

## 🚀 Hướng dẫn Cài đặt

1. **Chuẩn bị môi trường:** 
   - Đảm bảo bạn đã cài đặt Android Studio (bản mới nhất dánh cho Jetpack Compose).
   - SDK tối thiểu là Android API 24 (hoặc tùy thuộc vào cấu hình).

2. **Clone mã nguồn (Repository) về máy tính:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   cd YTSummary
   ```

3. **Chạy Build và cài đặt trên Simulator/Device:**
   - Mở thư mục dự án bằng Android Studio.
   - Chọn "Sync Project with Gradle Files". Lưu ý quá trình này sẽ tải framework Chaquopy từ Maven, cũng như cài đặt Python 3.12 vào thiết bị ảo.
   - Nhấn Cờ-lê (Build) -> Make Project và nhấn Run (`Shift + F10`).

## 📚 Hướng dẫn sử dụng

1. **Tích hợp API Key:** 
   - Truy cập trang [Google AI Studio](https://aistudio.google.com/) để lấy các Gemini API keys.
   - Mở ứng dụng `YTSummary`, nhấn vào góc Cài Đặt (Hình bánh răng).
   - Tại dòng "Add Gemini API Keys", dán thẳng danh sách tài khoản API hoặc text (Ứng dụng sử dụng Regex để bóc tách key hợp lệ, loại trừ mọi text rác).
2. **Tóm tắt Video:** 
   - Mở Youtube, Sao chép (Copy) Link Video.
   - Vào giao diện chính, bấm Nút `Paste & Tóm tắt nhanh`. Chờ đợi trong vài giây!

## ⚠️ Lưu ý về Bảo Mật & API (Gemini API Leak)

* Dự án **tuyệt đối không tải/lưu trữ/code cứng (hardcode)** API keys lên bất cứ file nào đẩy lên Github. 
* Toàn bộ API keys phải được quản lý và thiết lập bằng tay ở phía Client khi mở App lên (lưu thông qua Encrypted Database nội bộ vào bộ nhớ điện thoại). 
* Nếu phát hiện bất kỳ API key nào bị "Leak", vui lòng Roll back Commit và thay đổi API Keys ngay lập tức.

## ⚖️ Bản quyền
Copyright 2026 Nguyễn Duy Trường
