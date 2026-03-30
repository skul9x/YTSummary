# YTSummary - Ứng dụng Tóm tắt Video YouTube bằng AI

YTSummary là một ứng dụng Android chuyên dụng giúp bạn tiết kiệm thời gian bằng cách tóm tắt nội dung video YouTube một cách thông minh và nhanh chóng. Sử dụng sức mạnh của Google Gemini API, ứng dụng cung cấp những bản tóm tắt chi tiết, bố cục rõ ràng và chuyên sâu.

## 🚀 Tính năng nổi bật

- **Tóm tắt thông minh:** Chuyển đổi bản ghi âm (transcript) của video thành các điểm chính, phân tích chi tiết.
- **Hỗ trợ Gemini Thinking:** Tận dụng các mô hình suy luận (reasoning models) mới nhất của Gemini để đưa ra các phân tích sâu sắc hơn.
- **Cơ chế xoay vòng API Key:** Hỗ trợ quản lý và tự động xoay tua nhiều API Key (Key Rotation) để tối ưu hóa quota và tránh bị giới hạn bởi Google.
- **Hỗ trợ đa ngôn ngữ:** Hoạt động tốt với video nhiều ngôn ngữ, bao gồm cả Tiếng Việt.
- **Lịch sử tóm tắt:** Lưu trữ các bản tóm tắt đã thực hiện để xem lại bất cứ lúc nào.
- **Tích hợp Python (Chaquopy):** Sử dụng thư viện Python mạnh mẽ để lấy transcript trực tiếp từ YouTube.

## 🛠 Cấu trúc dự án

- `app/src/main/java`: Chứa mã nguồn logic xử lý bằng Kotlin và giao diện Jetpack Compose.
  - `api`: Giao diện kết nối với Gemini API.
  - `manager`: Quản lý API Key, Quota và Model.
  - `ui`: Các màn hình ứng dụng (Home, Settings, History, Summary).
- `app/src/main/python`: Chứa script Python (`yt_transcript_helper.py`) để trích xuất transcript.
- `.brain`: Lưu trữ ký ức và bối cảnh của dự án (Eternal Context).
- `docs/`: Chứa các tài liệu thiết kế và sửa lỗi.

## 📋 Yêu cầu hệ thống

- **Android:** API 26 (Android 8.0) trở lên.
- **Môi trường build:**
  - Android Studio Ladybug hoặc mới hơn.
  - Java JDK 17.
  - Python 3.11+ được cài đặt trên máy để hỗ trợ build Chaquopy.

## ⚙️ Hướng dẫn cài đặt

1.  **Clone repository:**
    ```bash
    git clone https://github.com/skul9x/YTSummary.git
    ```
2.  **Mở dự án:** Mở thư mục dự án bằng Android Studio.
3.  **Cấu hình API Key:**
    - Khởi chạy ứng dụng.
    - Truy cập màn hình **Cài đặt (Settings)**.
    - Thêm một hoặc nhiều Gemini API Key (Bắt đầu bằng `AIza...`).
4.  **Build và Run:** Nhấn nút Run trong Android Studio để cài đặt lên thiết bị hoặc giả lập.

## 📖 Cách sử dụng

1.  Mở ứng dụng YTSummary.
2.  Dán URL video YouTube bạn muốn tóm tắt vào ô nhập liệu hoặc chia sẻ trực tiếp từ ứng dụng YouTube.
3.  Chọn mô hình AI mong muốn (mặc định là Gemini 1.5 Flash).
4.  Nhấn nút "Tóm tắt" và đợi kết quả hiện ra.
5.  Kết quả sẽ được tự động lưu vào tab **Lịch sử**.

## ⚖️ Bản quyền

Copyright 2026 Nguyễn Duy Trường

---
*Tạo bởi Antigravity Workflow Framework*
