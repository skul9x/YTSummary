# YTSummary - Trình tóm tắt Video YouTube 🚀

YTSummary là ứng dụng Android mạnh mẽ giúp bạn tóm tắt nội dung video YouTube một cách thông minh, tiết kiệm thời gian và nâng cao hiệu suất học tập, làm việc. Ứng dụng tích hợp công nghệ trí tuệ nhân tạo tiên tiến (Google Gemini AI) để phân tích phụ đề video (transcript) và cung cấp các bản tóm tắt chi tiết, dễ đọc.

## 🌟 Tính năng nổi bật

- **Tóm tắt bằng AI (Google Gemini):** Phân tích và nắm bắt các điểm chính, bối cảnh từ phụ đề video YouTube.
- **Tách phụ đề nguyên bản (Native Transcript Fetching):** Lấy phụ đề trực tiếp tốc độ cao ngay trên thiết bị bằng Kotlin Native không phụ thuộc vào Python runtime, giúp tối ưu phần gốc và truy xuất cực nhanh.
- **Hiển thị thời gian thực (Streaming):** Các câu chữ phân tích được vẽ trực tiếp và liên tục nhờ Streaming SSE.
- **Hoạt động ngoại tuyến (Offline-First):** Lịch sử các clip đã được tóm tắt đều được lưu cục bộ với Room Database và tải siêu tốc với Paging 3, bạn có thể xem lại bất cứ khi nào.
- **Trợ lý đọc (Text-to-Speech):** Tính năng tự động đọc đoạn tóm tắt với giọng nói tự nhiên, rảnh tay để bạn nghe nội dung chính.
- **Tối ưu Network:** Cơ chế liên tục Exponential Backoff tự động đối phó lúc mạng yếu hay load 5xx.
- **Quản lý API linh hoạt:** Tự động xoay vòng nhiều khóa API để tối đa hóa quota và chống kiệt suất do Too Many Requests (429).

## 🛠️ Công nghệ sử dụng

YTSummary kết hợp các kiến trúc hàng đầu (Clean Architecture):
- **Giao diện (UI):** Jetpack Compose theo mẫu MVVM.
- **Lưu trữ Cục bộ (Local):** Room SQLite Database (với các index tối ưu) và Paging 3.
- **Mạng (Network):** Retrofit2, OkHttp3 (có Connection Pooling & Retry Interceptor tùy chỉnh).
- **Backend Services:** InnerTube API (cho việc tải nội dung YT Track) và Google Gemini API (dưới dạng LLM Summarizer).

## 📂 Cấu trúc thư mục

```
YTSummary/
├── app/src/main/java/com/skul9x/ytsummary/
│   ├── api/          # Quản lý giao tiếp API với Google Gemini LLM
│   ├── database/     # Cấu trúc Entities, DAOs của Room Database
│   ├── manager/      # Quản lý vòng quay model AI & API Keys
│   ├── network/      # HTTP Configs, Interceptors, Caching
│   ├── transcript/   # Trích xuất và phân tích YouTube Captions (Kotlin Native)
│   └── ui/           # Các màn hình Compose UI (Settings, History, Summary)
├── app/src/test/     # Các trình kiểm thử hệ thống Unit & Integration
├── plans/            # Hồ sơ tài liệu tiến trình triển khai tính năng
├── scripts/          # Các tập lệnh độc lập (JVM test scripts)
└── .brain/           # Môi trường lưu trữ trí thức AI nội bộ (AWF Workflow)
```

## 🚀 Hướng dẫn cài đặt

1. **Sao chép dự án (Clone):**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở dự án với Android Studio:** Yêu cầu các phiên bản mới (như Ladybug, Koala trở lên).
3. **Thiết lập Khóa (API Key):**
   - Lấy ít nhất một API Key từ [Google AI Studio](https://aistudio.google.com/).
   - Mở ứng dụng lần đầu, vuốt đến trang **Cài đặt** và nhập khóa API của bạn vào để tiến hành tóm tắt. Càng nhập nhiều khóa càng tự động xoay tua đa luồng linh hoạt!

## 💡 Cách sử dụng

1. **Lấy liên kết video:** Mở và tìm video dài / tài liệu bất kỳ trên YouTube.
2. **Khởi Động Nhanh:** Bấm chia sẻ đường link (Share Link) qua ứng dụng YTSummary, hoặc bạn cũng có thể mở trực tiếp App và dán đường link thủ công.
3. **Phân Tích AI:** Ứng dụng sẽ lọc và lấy tất cả phụ đề của Video (ưu tiên tiếng Việt - Anh), sau đó đưa cho mô hình Google Gemini để lên sườn ý chính.
4. **Theo dõi và Lưu trữ:** Bạn có thể nghe qua tính năng phát âm thanh toàn bài. Trong lần mở tới bài viết đã lưu sẽ tồn tại trong danh sách **Lịch sử tóm tắt**.

## 🤝 Đóng góp

Rất hoan nghênh chia sẻ mới! Nếu bạn có cải tiến nào hữu ích (UX/UI, Network, hay Database), vui lòng gửi **Pull Request** hoặc báo cáo sự cố qua **Issues**. Mọi đánh giá đều có ý nghĩa to lớn để phần mềm phục vụ tốt cho chất lượng tự trau dồi trên toàn quốc!

## ⚖️ Bản quyền

Copyright 2026 Nguyễn Duy Trường
