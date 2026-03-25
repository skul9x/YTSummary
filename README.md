# YTSummary

## 🤖 Tổng quan Dự án
**YTSummary** là một ứng dụng Android mạnh mẽ, biến chiếc điện thoại của bạn thành một trợ lý AI thông minh có khả năng tóm tắt video YouTube một cách thần tốc! 

Ứng dụng được thiết kế tối ưu với kiến trúc xử lý song song:
- **Tải phụ đề cục bộ:** Sử dụng công nghệ Chaquopy để chạy kịch bản Python trực tiếp trên Android, giúp lấy Transcript nhanh chóng mà không bị giới hạn bởi nhà mạng hay máy chủ trung gian.
- **Phân tích bằng Gemini AI:** Tích hợp các mô hình Gemini mới nhất (Flash/Preview) với chế độ Stream (SSE), cung cấp kết quả tóm tắt mượt mà và sâu sắc ngay lặp tức.
- **Bảo mật tối đa:** Toàn bộ API Key và dữ liệu nhạy cảm được mã hóa bằng SQLCipher + Room Database, bảo vệ thông tin cá nhân của bạn tuyệt đối.

## ✨ Tính năng Chính
- 🚀 **Trích xuất phụ đề siêu tốc:** Hỗ trợ đa ngôn ngữ (Tiếng Việt, Tiếng Anh) thông qua Python tích hợp.
- 🧠 **Tóm tắt thông minh:** Sử dụng hệ thống xoay tua model Gemini (3.1/3.0 Preview) để tối ưu hóa hạn ngạch và chất lượng.
- ⚡ **Giao diện Streaming SSI:** Trải nghiệm "chữ chạy" thời gian thực cực kỳ sinh động.
- 🎧 **Auto-TTS (Đọc tự động):** Tự động đọc bản tóm tắt sau khi hoàn thành, hỗ trợ AudioFocus (tự động giảm âm lượng nhạc nền).
- 🖱️ **1-Click Share:** Chia sẻ trực tiếp link từ ứng dụng YouTube vào YTSummary để bắt đầu tóm tắt ngay.
- 🔐 **An toàn dữ liệu:** Hệ thống lọc API Key thông minh và lưu trữ mã hóa chuẩn quân đội.

## 🛠️ Hướng dẫn Cài đặt
1. **Clone Repository:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở bằng Android Studio:** Sử dụng phiên bản Ladybug hoặc mới hơn. Đảm bảo bạn đã cài đặt JDK 17+.
3. **Cấu hình Python:** Trong lần build đầu tiên, Gradle sẽ tự động tải và cấu hình môi trường Python cho Chaquopy.
4. **Thiết lập API Key:**
   - Truy cập [Google AI Studio](https://aistudio.google.com/) để lấy API Key miễn phí.
   - Mở ứng dụng, vào phần **Cài đặt** và dán danh sách key của bạn.

## 📁 Cấu trúc Thư mục
```text
YTSummary/
├── app/
│   ├── src/main/java/              # Code Kotlin + Jetpack Compose (Giao diện & Logic)
│   ├── src/main/python/            # Script Python xử lý transcript
│   ├── src/main/res/               # Tài nguyên thiết kế, Adaptive Icon
├── .brain/                         # Kiến thức cốt lõi và lịch sử phát triển của AI trợ lý
├── docs/                           # Tài liệu kỹ thuật và báo cáo audit bảo mật
├── plans/                          # Kế hoạch phát triển các tính năng mới
└── README.md                       # Tài liệu hướng dẫn sử dụng
```

## 📜 Thông tin Bổ sung
Dự án được xây dựng với triết lý "Vibe Coding" - chú trọng vào cả hiệu năng lẫn thẩm mỹ cực cao (Rich Aesthetics).

## ⚖️ Bản quyền
Copyright 2026 Nguyễn Duy Trường
