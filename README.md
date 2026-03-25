# 🤖 YT Summary AI - Trợ Lý Tóm Tắt Video Thông Minh

**YT Summary AI** là một ứng dụng Android hiện đại, mạnh mẽ, giúp bạn nắm bắt nội dung video YouTube chỉ trong vài giây thông qua sức mạnh của trí tuệ nhân tạo (Gemini AI).

---

## ✨ Tính năng Nổi bật

*   🚀 **Tóm tắt Siêu tốc:** Tự động trích xuất phụ đề (Transcript) và tóm tắt nội dung chính xác.
*   🧠 **Đa mô hình AI Gen (Gemini):** Xoay tua thông minh giữa các mô hình mạnh mẽ của Google (Gemini 3.1 Flash, 2.5 Flash...) để tối ưu hóa hạn ngạch.
*   ⚡ **Streaming SSI:** Hiển thị kết quả tóm tắt theo thời gian thực (chữ chạy) sinh động.
*   🎧 **Auto-TTS (Đọc văn bản):** Tích hợp công cụ đọc tự động (Text-to-Speech) giúp bạn nghe tóm tắt khi đang bận.
*   💎 **Giao diện Glassmorphism:** Thiết kế Premium với hiệu ứng kính mờ và Neon cực kỳ thẩm mỹ.
*   📂 **Lịch sử tóm tắt:** Lưu trữ và quản lý các video đã tóm tắt để xem lại bất cứ lúc nào.
*   🔄 **Tự động kiểm tra cập nhật:** Thông báo khi có phiên bản thư viện Python mới để đảm bảo tính ổn định.
*   🛠️ **Quản lý Model Priority:** Tùy biến thứ tự ưu tiên các mô hình Gemini, thiết lập cơ chế Fallback linh hoạt và kiểm tra kết nối (Connectivity Test) ngay trong ứng dụng.

---

## 🛠️ Công nghệ Sử dụng

*   **Android:** Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow.
*   **Python (Chaquopy):** Sử dụng thư viện `youtube-transcript-api` chạy trực tiếp trên Android để lấy dữ liệu transcript.
*   **AI Engine:** Google Gemini AI API (với cơ chế xoay tua khóa API an toàn).
*   **Database:** Room Database lưu trữ lịch sử được mã hóa.

---

## 📁 Cấu trúc Thư mục

```text
YTSummary/
├── app/
│   ├── src/main/java/              # Toàn bộ logic ứng dụng (UI, Manager, Api)
│   ├── src/main/python/            # Script Python xử lý lấy Transcript video
│   ├── src/main/res/               # Tài nguyên thiết kế (Layout, Icon, Color)
├── .brain/                         # Dữ liệu kiến thức và lịch sử phát triển của AI trợ lý
├── docs/                           # Tài liệu thiết kế hệ thống và báo cáo sơ bộ
├── plans/                          # Kế hoạch phát triển và lộ trình các tính năng
└── README.md                       # Tài liệu hướng dẫn sử dụng (bản hiện tại)
```

---

## 🚀 Hướng dấn Cài đặt

1.  **Clone dự án:**
    ```bash
    git clone https://github.com/skul9x/YTSummary.git
    ```
2.  **Mở bằng Android Studio:** (Khuyên dùng bản Ladybug trở lên).
3.  **Cấu hình API Key:**
    *   Lấy API Key từ [Google AI Studio](https://aistudio.google.com/).
    *   Trong ứng dụng, vào mục **Cài đặt** -> **Thêm API Key**.
4.  **Chạy ứng dụng:** Nhấn `Run` để cài đặt lên thiết bị Android (Yêu cầu API 24+).

---

## ⚠️ Lưu ý Bảo mật

*   Tuyệt đối **không** chia sẻ API Key của bạn trong repository Github.
*   Dự án sử dụng `EncryptedSharedPreferences` để lưu trữ Key của người dùng một cách an toàn nhất trên bộ nhớ máy.

---

## 📜 Thông tin Bổ sung
Dự án được phát triển theo triết lý **"Vibe Coding"**, mang lại trải nghiệm người dùng mượt mà và trực quan nhất có thể.

---

## ⚖️ Bản quyền
Copyright 2026 Nguyễn Duy Trường
