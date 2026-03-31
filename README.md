# YTSummary - Trình tóm tắt Video YouTube thông minh 🚀

YTSummary là một ứng dụng Android chuyên dụng giúp bạn tiết kiệm thời gian bằng cách tóm tắt nội dung video YouTube một cách thông minh và nhanh chóng. Sử dụng sức mạnh của **Google Gemini AI**, ứng dụng cung cấp những bản tóm tắt chi tiết, bố cục rõ ràng và chuyên sâu.

## 🌟 Tính năng nổi bật

- **Tóm tắt thông minh:** Chuyển đổi bản ghi âm (transcript) của video thành các điểm chính, phân tích chi tiết.
- **⚡ Streaming AI:** Xem kết quả đang được tạo ra theo thời gian thực (Real-time).
- **🧠 Hỗ trợ Gemini Thinking:** Tận dụng các mô hình suy luận (reasoning models) mới nhất để đưa ra các phân tích sâu sắc hơn.
- **🕒 Lịch sử tóm tắt:** Lưu trữ và xem lại các video đã tóm tắt ngoại tuyến bất cứ lúc nào (Offline first).
- **🔊 Text-To-Speech:** Hỗ trợ đọc to bản tóm tắt bằng giọng nói tự nhiên.
- **🛠️ Cơ chế xoay vòng API Key:** Hỗ trợ quản lý và tự động xoay tua nhiều API Key để tối ưu hóa quota.
- **📊 Tối ưu hóa hiệu năng:** Sử dụng Paging 3 và Room Database cho trải nghiệm mượt mà.

## 🛠️ Công nghệ sử dụng

- **Android:** Jetpack Compose, Room SQLite, Paging 3, Retrofit & OkHttp (với Connection Pooling và Retry Interceptor).
- **Python (Chaquopy):** Sử dụng `youtube-transcript-api` để trích xuất transcript trực tiếp trên mobile.
- **AI Engine:** Google Gemini SDK với kỹ thuật streaming SSE dữ liệu thô.

## 📂 Cấu trúc dự án tiêu biểu

- `app/src/main/java`: Mã nguồn ứng dụng Android (Kotlin/Compose).
  - `api`: Giao diện kết nối với Gemini API.
  - `manager`: Quản lý API Key, Quota và Model.
  - `ui`: Giao diện người dùng hiện đại.
- `app/src/main/python`: Logic backend trích xuất transcript bằng Python.
- `/.brain`: Kho tri thức dự án và ngữ cảnh phát triển sản phẩm (Eternal Context).
- `docs/`: Tài liệu thiết kế và hướng dẫn phát triển.

## 🚀 Hướng dẫn cài đặt

1.  **Clone dự án:**
    ```bash
    git clone https://github.com/skul9x/YTSummary.git
    ```
2.  **Mở bằng Android Studio:** (Bản Ladybug hoặc mới hơn).
3.  **Cấu hình API Key:** 
    - Khởi chạy ứng dụng.
    - Truy cập màn hình **Cài đặt (Settings)** và thêm API Key của bạn.
4.  **Cấu hình Python:** Chaquopy sẽ tự động xử lý các dependencies khi build.

## 📝 Quy tắc đóng góp
Chúng tôi chào đón mọi đóng góp! Vui lòng tạo **Issue** hoặc gửi **Pull Request** nếu bạn có ý tưởng mới.

## ⚖️ Bản quyền
**Copyright 2026 Nguyễn Duy Trường**

---
*Phát triển bởi Nguyễn Duy Trường - 2026*
