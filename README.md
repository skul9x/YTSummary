# YTSummary (AI Video Summarizer)

**YTSummary** là một ứng dụng Android thông minh, sử dụng AI (Gemini 2.5 Flash) để tự động trích xuất và tóm tắt nội dung (subtitle) của các video trên YouTube. Ứng dụng hoạt động vượt qua các giới hạn khóa bằng IP của trung tâm dữ liệu thông qua cơ chế chạy mã Python nội bộ trực tiếp trên điện thoại (Chaquopy). Vừa lấy phụ đề nhanh chóng, vừa bảo mật tối đa!

---

## 🚀 Tính năng nổi bật

- **Tóm tắt siêu tốc:** Lấy video ID và dùng Gemini API để bóc tách ý chính xác. Cung cấp cả suy luận tư duy chiều sâu.
- **Trích xuất Subtitle Cục bộ (Local Fetch):** Tích hợp thư viện Python `youtube-transcript-api` chạy ngầm thông qua bridge *Chaquopy*, giúp tránh hoàn toàn các lỗi `502 Bad Gateway` hay block IP mà các máy chủ cloud thường gặp.
- **Chia sẻ tự động 1 chạm (Share Intent):** Mở ứng dụng YouTube, nhấn "Chia sẻ" vào YTSummary, ứng dụng sẽ chạy tự động từ khâu bắt link, lấy transcript, tóm tắt và thực hiện luôn việc đọc thành lời (Auto Text-to-Speech) rảnh tay 100%.
- **Bảo mật mạnh mẽ:**
  - Quản lý API Key Gemini thông minh.
  - Mã hoá dữ liệu cơ sở trên máy bằng công nghệ **Room + SQLCipher (Encrypted)**.
  - Ngăn chặn mọi rò rỉ Key thông qua Strict API Validation và chặn log bừa bãi.
- **Tối ưu UX / Giao diện người dùng:** Sử dụng giao diện Jetpack Compose mượt mà, hỗ trợ Animation trạng thái tải, hiệu ứng kính mờ (Glassmorphism) cực đẹp mắt.

---

## 🛠 Hướng dẫn Cài đặt

1. **Yêu cầu môi trường:**
   - Android Studio (Electric Eel trở lên / Flamingo / Hedgehog / Iguana...).
   - Android SDK API 34, JDK 17.
   - Thiết bị thật hoặc máy ảo x86_64, arm64-v8a (Do sử dụng Chaquopy).

2. **Cách build ứng dụng:**
   - Clone repository về máy tính:
     ```bash
     git clone https://github.com/skul9x/YTSummary.git
     ```
   - Mở dự án trong Android Studio.
   - Quá trình Sync Gradle sẽ tải xuống `Chaquopy 17.0.0` và cài môi trường Python 3.11 vào project. *(Sẽ kéo dài một vài phút cho lần build đầu tiên).*
   - Build ứng dụng: Click `Run (Shift + F10)` để cài qua USB/Emulator hoặc chạy mã trên Terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 💡 Cách sử dụng

Ứng dụng cực kỳ tối giản với luồng cơ chế ưu tiên cho người dùng lấy kết quả nhanh nhất:

**Cách 1: Sao chép dán trực tiếp**
1. Mở ứng dụng `YTSummary` và điền API Key (Gemini) của bạn ở mục Settings.
2. Dán đường dẫn YouTube vào ô nhập (hoặc bấm nút *Paste & Tóm tắt nhanh* để lấy link từ khay nhớ tạm).
3. Đợi tiến trình Loading lấy chữ (5 - 10 giây).
4. Xem kết quả tóm tắt hoặc bấm biểu tượng Loa để ứng dụng đọc cho bạn báo cáo chi tiết (TTS).

**Cách 2: "1-Click" qua chức năng Share (Đề xuất)**
1. Khi đang xem một video hay trên app YouTube, nhấn menu **Chia sẻ (Share)**.
2. Tại danh sách ứng dụng, chọn biểu tượng của **YTSummary**.
3. Bỏ điện thoại xuống! YTSummary sẽ tự mở lên ngầm, tự bóc tách link, tự tải AI và tự đọc to phụ đề tóm tắt (TTS) mà bạn không cần chạm màn hình thêm phát nào nữa.

---

## 📁 Cấu trúc Thư mục

- `app/src/main/java/com/skul9x/ytsummary`
  - `ui/`: Tầng giao diện viết bằng Compose UI (MainActivity, Component, Theme).
  - `repository/`: Các file quản lý Luồng (Flow / Async) giao tiếp gọi API và Python nội bộ.
  - `api/gemini/`: Nơi cài đặt call model `gemini-2.5-flash` và Prompt injection filter.
  - `manager/`: Quản lý TTS (Thuyết minh), ModelQuota (Phần trăm sử dụng/khóa).
  - `model/`: Các file cấu trúc dữ liệu chính (Data Classes).
- `app/build.gradle.kts`: Chứa các script cầu nối Chaquopy và Pip dependencies.
- `.brain/`: Thư mục lưu trữ context và ghi nhớ kiến thức dự án chuyên sâu.

---

## 📜 Bản quyền

Copyright 2026 Nguyễn Duy Trường
