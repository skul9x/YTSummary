# 📺 YTSummary - Trình tóm tắt video YouTube thông minh 🚀

**YTSummary** là ứng dụng Android tối ưu, được thiết kế để giúp bạn nắm bắt nội dung video YouTube chỉ trong vài giây. Bằng cách kết hợp sức mạnh của trí tuệ nhân tạo **Gemini 2.5 Flash** và khả năng xử lý cục bộ mạnh mẽ qua **Chaquopy**, YTSummary mang đến trải nghiệm tóm tắt video mượt mà, bảo mật và cực kỳ hiệu quả.

---

## ✨ Tính năng nổi bật

*   **⚡ Local Transcript Extraction**: Ứng dụng tích hợp Python (qua Chaquopy) để lấy phụ đề (subtitle) trực tiếp từ YouTube, không cần qua server trung gian, đảm bảo tốc độ và bảo mật thông tin.
*   **🤖 AI Summarization (Gemini 2.5 Flash)**: Tận dụng mô hình AI mới nhất để tóm tắt nội dung video sâu sắc, nắm bắt đúng ý chính và cấu trúc bài nói.
*   **🌊 SSE Streaming UX**: Hiển thị kết quả tóm tắt dạng nhảy chữ thời gian thực (như ChatGPT), mang lại cảm giác phản hồi tức thì.
*   **🗣️ Auto-TTS Sentence Chunking**: Bộ đọc văn bản (Text-to-Speech) thông minh, bắt đầu đọc ngay khi câu đầu tiên được AI tạo ra, không cần chờ đợi toàn bộ bản tóm tắt hoàn thành.
*   **🔊 AudioFocus & Ducking**: Hệ thống âm thanh chuyên nghiệp, tự động giảm âm lượng nhạc nền (ducking) khi AI đang đọc tóm tắt và khôi phục khi kết thúc.
*   **📂 Offline Cache (Room + SQLCipher)**: Tự động lưu trữ lịch sử tóm tắt vào cơ sở dữ liệu được mã hóa hoàn hoàn. Bạn có thể xem lại kết quả ngay lập tức mà không cần kết nối internet.
*   **🔄 Auto Update Notification**: Tự động kiểm tra phiên bản mới nhất của thư viện Python trên PyPI và hiển thị cảnh báo cập nhật trực quan trên giao diện ứng dụng.
*   **📲 Share Intent Integration**: Chia sẻ video trực tiếp từ ứng dụng YouTube sang YTSummary để tóm tắt trong 1 cú click. Tự động xử lý khi app đang chạy ngầm (`singleTop`).
*   **🎨 Glassmorphism Design**: Giao diện mang đậm phong cách hiện đại với hiệu ứng kính mờ, thẻ GlassCard sang trọng và icon thích ứng (Adaptive Icons).

---

## 🛠️ Công nghệ cốt lõi

| Công nghệ | Vai trò |
|-----------|---------|
| **Kotlin (Compose)** | Ngôn ngữ phát triển UI hiện đại, mượt mà. |
| **Chaquopy** | Cầu nối chạy Python 3.12 trực tiếp bên trong Android. |
| **Gemini AI** | Trái tim xử lý ngôn ngữ tự nhiên từ Google. |
| **Room + SQLCipher** | Lưu trữ dữ liệu an toàn cao cấp với mã hóa AES-256 đầu cuối. |
| **OkHttp & Retrofit** | Xử lý các luồng dữ liệu mạng và SSE Streaming. |
| **MockWebServer** | Đảm bảo tính ổn định và độ tin cậy của code qua Unit Testing. |

---

## 📂 Cấu trúc dự án

```text
YTSummary-main/
├── app/src/main/java       # Mã nguồn Kotlin (UI, Manager, Api, Logic)
├── app/src/main/python     # Script Python lấy phụ đề (yt_transcript_helper.py)
├── app/src/test/java       # Unit test (xác thực logic API/Checker)
├── .brain/                 # 🧠 Bộ nhớ dự án (dành cho Antigravity AI Assistant)
├── docs/                   # Tài liệu chi tiết về kiến trúc (Audit, Specs)
└── app/build.gradle.kts    # Cấu hình dự án & Dependencies (PIP packages)
```

---

## ⚙️ Hướng dẫn cài đặt

1.  **Clone dự án**:
    ```bash
    git clone https://github.com/skul9x/YTSummary.git
    ```
2.  **Mở bằng Android Studio**: Yêu cầu phiên bản Android Studio Ladybug hoặc mới hơn.
3.  **Cấu hình dự án**:
    *   Đảm bảo máy đã cài đặt Python 3.11/3.12 (để Gradle cài Chaquopy).
    *   Nhấn **Sync Project with Gradle Files**.
4.  **Chạy ứng dụng**: Kết nối thiết bị Android (minSdk 26) và nhấn **Run (Shift + F10)**.

---

## 📖 Cách sử dụng

1.  **Thiết lập API Key**: Mở ứng dụng ➔ Vào **Settings** ➔ Dán Gemini API Key của bạn (App sẽ tự bóc tách Key từ bất kỳ đoạn văn bản nào bạn dán vào).
2.  **Tóm tắt video**:
    *   **Cách 1**: Dán link YouTube trực tiếp trên màn hình chính.
    *   **Cách 2**: Khi đang xem video trong app YouTube ➔ nhấn nút **Share (Chia sẻ)** ➔ chọn **YTSummary**.
3.  **Nghe tóm tắt**: Nhấn biểu tượng loa để AI đọc to nội dung tóm tắt cho bạn.

---

## 📜 Bản quyền

Copyright 2026 Nguyễn Duy Trường

---
*Phát triển bởi [skul9x] với sự hỗ trợ của Antigravity Workflow Framework (AWF) – Bảo mật, hiệu quả và tối ưu.*
