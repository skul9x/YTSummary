# 🤖 YTSummary AI - Tóm tắt Video YouTube bằng Trí tuệ Nhân tạo

**YTSummary AI** là một ứng dụng Android hiện đại, mạnh mẽ, được thiết kế để giúp bạn tiết kiệm thời gian bằng cách tóm tắt nội dung video YouTube một cách nhanh chóng và chính xác thông qua công nghệ Gemini AI của Google. 

---

## ✨ Tính năng nổi bật

- 📝 **Tóm tắt AI thông minh**: Sử dụng các mô hình Gemini mới nhất (Flash, Pro) để tạo ra các bản tóm tắt súc tích, dễ hiểu.
- 🔄 **Cơ chế Model Rotation**: Tự động xoay vòng giữa danh sách các API Key và Model để tối ưu hóa quota và độ tin cậy.
- 🔊 **Hỗ trợ TTS (Text-to-Speech)**: Đọc to nội dung tóm tắt để bạn có thể lắng nghe khi đang di chuyển.
- 🎨 **Giao diện Glassmorphism**: Thiết kế hiện đại theo phong cách "Vibe Coding" với hiệu ứng kính mờ và Neon bắt mắt.
- 🔒 **Bảo mật tuyệt đối**: API Key được mã hóa và lưu trữ an toàn bằng `EncryptedSharedPreferences`.
- 📂 **Quản lý lịch sử**: Tự động lưu trữ các bản tóm tắt trước đó trong cơ sở dữ liệu nội bộ (Room).

---

## 🛠 Cấu trúc dự án

Dự án bao gồm 3 phần chính:

1.  **`app/`**: Mã nguồn ứng dụng Android (Kotlin, Jetpack Compose, Material 3).
2.  **`backend/`**: Backend xử lý trung gian dựa trên FastAPI (Python).
3.  **`youtube-transcript-api-master/`**: Thư viện xử lý trích xuất phụ đề YouTube được tinh chỉnh cục bộ.

---

## 🚀 Hướng dẫn cài đặt & Sử dụng

### 1. Backend (FastAPI)
- Yêu cầu: Python 3.9+
- Cài đặt dependency: `pip install -r backend/requirements.txt`
- Chạy server: `uvicorn backend.main:app --reload`

### 2. Frontend (Android)
- Mở thư mục `app/` bằng Android Studio.
- Cấu hình API Key trong mục **Settings (Cài đặt)** ngay trên ứng dụng.
- Nhập link video YouTube và nhấn biểu tượng "Play" để nhận kết quả.

---

## 🏗 Công nghệ sử dụng

- **Mobile**: Kotlin, Jetpack Compose, Retrofit, Room, Dagger Hilt.
- **AI**: Google Gemini API (Vertex AI / Google AI Studio).
- **Backend**: Python, FastAPI, YouTube Transcript API.
- **Design**: Figma Design System (Glassmorphism & Neon styles).

---

## 📜 Bản quyền & Liên hệ

**Copyright 2026 Nguyễn Duy Trường**

Dự án được phát triển với mục đích học tập và chia sẻ kiến thức. Vui lòng ghi rõ nguồn nếu bạn có ý định sử dụng lại mã nguồn.

---
> *Tự động được cập nhật và làm sạch mã nguồn bởi Antigravity AI.*
