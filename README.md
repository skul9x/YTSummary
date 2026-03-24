# YTSummary (YouTube AI Summarizer) 📺💡

Ứng dụng Android hỗ trợ lấy phần phụ đề của video YouTube và tóm tắt nội dung cực kỳ nhanh chóng bằng AI (Google Gemini), sử dụng công nghệ xử lý nhúng Python cục bộ (Chaquopy) trên chính thiết bị của bạn.

## 🚀 Tính năng nổi bật
- **Trích Xuất Phụ Đề Cục Bộ**: Ứng dụng tích hợp thư viện `youtube-transcript-api` của Python thông qua Chaquopy, giúp thiết bị tự động tải phụ đề về mà không phải đi qua server trung gian (Tránh triệt để rào cản cấm IP server/bot từ YouTube).
- **Tóm Tắt Thông Minh Bằng AI**: Sự kết hợp hoàn hảo cùng sức mạnh của Google Gemini API (hỗ trợ cả các mô hình xịn nhất như Gemini 2.5 Flash), chắt lọc thông tin video ngắn gọn, súc tích.
- **Audio & Text-to-Speech (TTS) Nhúng**: Không chỉ đọc nội dung văn bản mà app còn có trình quản lý âm thanh (auto volume control) và tự động đọc lại bài tóm tắt.
- **Giao Diện Glassmorphism**: Trang bị bộ UI Compose theo phong cách kính mờ cực đẹp và hiện đại, tối ưu hóa màu sắc cho Dark Mode.
- **Bảo Mật API Keys Cao**: Sử dụng MasterKey và EncryptedSharedPreferences để quản lý mã API Keys, tránh rò rỉ hoặc đánh cắp.
- **Chống Prompt Injection**: Áp dụng các quy tắc bảo mật cao nhất, bao gồm chốt chặt rule nhận diện nội dung rác và ép cấu hình Safety Filters cao su cho nội dung đọc để tránh các đoạn script lừa đảo ngầm.

## 🛠 Hướng dẫn Cài đặt
1. Mở dự án bằng **Android Studio**.
2. Dự án sử dụng cấu trúc **Gradle Kotlin DSL**. Mở file, chờ Sync các thư viện như Compose, Retrofit (nếu có), OkHttp, Room và đặc biệt là bộ **Chaquopy 17.0.0**.
3. Tại ứng dụng, đi đến mục cấu hình/Cài đặt API và dán các API Keys của Gemini (hỗ trợ nhập đoạn text rối, ứng dụng sẽ tự trích xuất key `AIza...`).
4. Bấm Run (`app-debug.apk`) qua giả lập hoặc điện thoại thực tế để trải nghiệm!

## 🔖 Cấu trúc thư mục (Nổi bật)
- `app/src/main/java/com/skul9x/ytsummary/`: Trái tim nền tảng của Kotlin (Chứa UI, State, Repository, System Managers).
- `app/src/main/python/`: Những file kịch bản Python chịu trách nhiệm giao tiếp giữa Youtube và Kotlin (Native App).
- `.brain/`: Thư mục đặc thù lưu trữ trí nhớ về toàn bộ cấu trúc dự án của AI Antigravity Worker (Giúp nắm bắt architecture, log làm việc).

---
## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường
