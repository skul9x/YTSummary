# 📱 YTSummary - Trợ lý AI Tóm Tắt Video YouTube

YTSummary là một ứng dụng Android thông minh giúp tóm tắt nội dung các video YouTube một cách nhanh chóng, ngắn gọn và dễ hiểu. Ứng dụng được xây dựng theo kiến trúc **Standalone Client**, tích hợp xử lý nội bộ (Local) với sức mạnh của **Google Gemini API** siêu việt.

---

## 🌟 Tính năng nổi bật
* **Trích xuất phụ đề (Transcript) cục bộ**: Sử dụng thư viện `youtube-transcript-api` thông qua môi trường Python nhúng (Chaquopy) chạy ngay lập tức trên máy. Giúp vượt rào hoàn toàn (bypass) các cơ chế chặn địa chỉ IP (IP Blocking) mà không đòi hỏi bất kỳ máy chủ backend/trung gian nào.
* **Tóm tắt siêu tốc - Cực kỳ sâu sắc**: Kích hoạt tư duy nâng cao (Thinking-capability) của model `gemini-2.5-flash` mới nhất từ Google, cô đọng nội dung video của bạn chỉ trong vài click.
* **Khoá tự động xoay vòng an toàn (API Key Rotation)**: Tự động phát hiện lỗi 429 (Hết hạn mức - Quota Exceeded), lỗi 503 (Server Busy) để luân chuyển key & model mượt mà mà không làm đứt đoạn tác vụ.
* **Auto Regex Paste**: Tự động nhận diện và bóc tách nhiều khóa API (API Key) từ cục nội dung bạn copy nháp trên máy → Nhanh chóng nạp đạn cho AI.
* **Lưu trữ Offline bảo mật**: Mọi bản tóm tắt đều được lưu về **Room Database 100% mã hóa (SQLCipher)** bảo mật tối đa riêng tư người dùng.
* **Nghe Audio (Voice Assistant)**: Trợ lý đọc nội dung tóm tắt rành mạch để bạn nghe kể chuyện hoặc nghe summary trong lúc đang lái xe.

---

## 🛠️ Công nghệ sử dụng 
- **Ngôn ngữ**: Kotlin, Python 3.12 (nhúng)
- **Giao diện**: Jetpack Compose (Phong cách Neon, Material Design 3, Glassmorphism UI).
- **Trình thông dịch AI/Python**: Chaquopy 17.0.0
- **Database**: Room Persistence Library kết hợp SQLCipher (Mã hóa chuẩn AES-256).
- **Mạng/API**: OkHttp3, API Gemini REST theo chuẩn kiến trúc Coroutines.

---

## 🚀 Hướng dẫn cài đặt và sử dụng

### 1. Yêu cầu & Chuẩn bị
* Môi trường phát triển: **Android Studio Ladybug** (hoặc mới nhất).
* Hệ điều hành: Tối thiểu SDK 26 (Android 8.0), mục tiêu (Target) SDK 34.
* Hãy lấy sẵn 1 hoặc nhiều **Gemini API Key** miễn phí từ *Google AI Studio*.

### 2. Cài đặt mã nguồn
- Tải Repository về thiết bị bằng Git:  
  `git clone https://github.com/skul9x/YTSummary.git`
- Mở thư mục bằng Android Studio và thực hiện lệnh cài đặt package: chạy **Sync Project with Gradle Files**.  
  *(**Lưu ý:** Vì đây là ứng dụng nhúng thêm Chaquopy Python pip, có thể mất vài phút tải môi trường vào `app/build/` trong lần đầu tiên chạy Gradle).*
- Build ứng dụng (Run App) để cài lên điện thoại của bạn.

### 3. Cách sử dụng App
1. **Thiết lập API**: Ngay khi vào ứng dụng, bạn bấm vào nút ⚙️ (Settings) trên trang chủ. Tại đây, hãy nhúng các khóa API bạn đã copy. Ứng dụng đủ khôn để tự động gom lấy các chuỗi `AIza...` hợp lệ vào hệ thống khóa an toàn. 
2. **Khai thác tóm tắt**: Tìm một Youtube Video tâm đắc, lấy url copy vào clipboard. Bấm nút **"📋 Paste & Tóm tắt nhanh"**.
3. **Thưởng thức**: Chờ khoảng 2-5 giây để quá trình trích xuất và tóm tắt diễn ra. Bạn có thể bấm Sao Chép hoặc bấm Nút Phát Âm Thanh (Voice Assistant) để nghe text.

---

## 📂 Cấu trúc thư mục cốt lõi
```text
YTSummary/
 ┣ app/
 ┃ ┣ src/main/java/com/skul9x/ytsummary/
 ┃ ┃ ┣ manager/          # Bridge Kotlin-Python, API Rotation & Tts Managers.
 ┃ ┃ ┣ repository/       # Data Layer liên kết Local DB (Room) & Remote API.
 ┃ ┃ ┗ ui/               # Màn hình Compose (Trang chủ, Lịch sử, Settings).
 ┃ ┣ src/main/python/     
 ┃ ┃ ┗ yt_transcript_helper.py    # Code Python khai thác OEmbed phụ đề.
 ┣ docs/                 # Các tài liệu hệ thống, API Docs bổ trợ. 
 ┣ .brain/               # Nhật ký, tài liệu context & Memory của AI Workspace.
 ┗ build.gradle.kts      # Cấu hình Gradle, nhúng Chaquopy Pip dependencies.
```

---

## 📜 Bản quyền
Copyright 2026 Nguyễn Duy Trường
