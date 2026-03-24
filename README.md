# YTSummary - Trình tóm tắt video YouTube bằng AI (Local & Standalone)

YTSummary là một ứng dụng Android hiện đại được xây dựng để giúp bạn tiết kiệm thời gian bằng cách tóm tắt nội dung video YouTube trực tiếp trên điện thoại. Điểm đặc biệt của ứng dụng là xử lý dữ liệu phụ đề hoàn toàn cục bộ (local) và tóm tắt qua Gemini AI với hiệu năng tối ưu.

## 🚀 Tính năng nổi bật
*   **Local Transcript Fetching**: Không cần server trung gian, ứng dụng sử dụng Chaquopy để chạy Python trực tiếp trên Android, giúp lấy phụ đề video một cách nhanh chóng và bảo mật.
*   **AI Summarization (Gemini)**: Sử dụng mô hình Gemini 2.5 Flash để phân tích và tóm tắt nội dung sâu sắc.
*   **SSE Streaming**: Hiển thị kết quả tóm tắt dạng stream (nhảy chữ) như ChatGPT, mang lại trải nghiệm mượt mà.
*   **TTS Sentence Chunking**: Bộ đọc văn bản (Text-to-Speech) bắt đầu đọc ngay khi câu đầu tiên được AI tạo ra, không cần chờ đợi.
*   **Offline Cache**: Tự động lưu trữ các bản tóm tắt vào database Room (đã mã hóa) để xem lại tức thì mà không cần mạng.
*   **Share Intent**: Chia sẻ link từ ứng dụng YouTube sang YTSummary để tóm tắt chỉ với 1 cú click.
*   **Giao diện Glassmorphism**: Thiết kế hiện đại, cao cấp với hiệu ứng kính mờ và hỗ trợ Adaptive Icon.

## 🛠️ Công nghệ sử dụng
*   **Ngôn ngữ**: Kotlin (Jetpack Compose)
*   **Cầu nối Python**: [Chaquopy](https://chaquo.com/python/)
*   **AI**: Google Gemini API
*   **Database**: Room + SQLCipher (Mã hóa đầu cuối)
*   **Networking**: OkHttp & Retrofit (Retrofit hiện chủ yếu dùng cho các API phụ)

## 📂 Cấu trúc thư mục chính
*   `app/src/main/java`: Mã nguồn Kotlin của ứng dụng Android.
*   `app/src/main/python`: Các script Python xử lý lấy phụ đề YouTube.
*   `.brain/`: Lưu trữ ngữ cảnh dự án (dùng cho AI Assistant).
*   `docs/`: Tài liệu chi tiết về kiến trúc hệ thống và API.

## ⚙️ Hướng dẫn cài đặt & Sử dụng
1.  **Clone dự án**: `git clone https://github.com/skul9x/YTSummary.git`
2.  **Mở bằng Android Studio**: Đảm bảo bạn đã cài đặt Android Studio Koala/Ladybug trở lên.
3.  **Cấu hình API Key**: Mở ứng dụng, vào phần Settings và nhập Gemini API Key của bạn.
4.  **Sử dụng**: Dán link video YouTube hoặc sử dụng tính năng "Share" trực tiếp từ YouTube.

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường

---
*Dự án được phát triển và tối ưu hóa bởi Antigravity Workflow Framework (AWF).*
