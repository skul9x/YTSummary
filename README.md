# YTSummary - Trình tóm tắt YouTube AI Standalone (Android)

YTSummary là ứng dụng Android hiện đại giúp tóm tắt nội dung video YouTube bằng trí tuệ nhân tạo (AI). Dự án đã chuyển đổi từ kiến trúc Cloud sang kiến trúc **Standalone (Local Python)** để đảm bảo tính ổn định và bảo mật cao nhất.

## 🌟 Tính năng chính

- **Tóm tắt thông minh:** Sử dụng model **Gemini 2.5 Flash** để tóm tắt nội dung video nhanh chóng, chính xác.
- **Kiến trúc Standalone (Chaquopy):** Tích hợp Python trực tiếp vào App Android để lấy transcript bằng IP của thiết bị, giúp vượt qua các rào cản chặn IP của YouTube đối với các Cloud Data Center.
- **Tối ưu hóa cho người lái xe:** Prompt được thiết kế đặc biệt theo phong cách ngắn gọn, súc tích, dễ nghe khi sử dụng tính năng đọc bằng giọng nói (Text-to-Speech).
- **Bảo mật dữ liệu:** Lịch sử tóm tắt được lưu trữ local và mã hóa bằng **Room Database + SQLCipher**.
- **Không cần Backend:** Không phụ thuộc vào máy chủ trung gian, giúp giảm chi phí vận hành và tăng tốc độ xử lý.

## 🛠️ Công nghệ sử dụng

- **Frontend:** Kotlin, Jetpack Compose, Material Design 3.
- **AI Integration:** Google Gemini API (models/gemini-2.5-flash).
- **Python Bridge:** Chaquopy 17.0.1 (Python 3.11).
- **Phụ đề:** `youtube-transcript-api`.
- **Cơ sở dữ liệu:** Room + SQLCipher (Mã hóa toàn diện).

## 📂 Cấu trúc thư mục

- `app/src/main/python/`: Chứa logic Python (`yt_transcript_helper.py`) để lấy transcript và metadata.
- `app/src/main/java/`: Chứa mã nguồn Kotlin của ứng dụng (UI, Manager, Repository).
- `.brain/`: Thư mục lưu trữ kiến thức dự án và context của AI (Dùng cho phát triển).
- `docs/`: Tài liệu mô tả kiến trúc và báo cáo bảo mật.

## 🚀 Hướng dẫn cài đặt

1. **Clone Repository:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```

2. **Mở dự án:** 
   Sử dụng Android Studio (phiên bản mới nhất) để mở thư mục dự án.

3. **Đồng bộ Gradle:**
   Nhấn **"Sync Project with Gradle Files"**. Lưu ý: Quá trình này sẽ tải về Python Runtime và các thư viện cần thiết (`youtube-transcript-api`, `requests`).

4. **Cấu hình API Key:**
   - Lấy API Key tại [Google AI Studio](https://aistudio.google.com/).
   - Nhập API Key vào phần cài đặt trong ứng dụng.

5. **Build & Run:**
   Build app và cài đặt lên điện thoại Android hoặc Emulator.

## ⚠️ Lưu ý quan trọng

- Dự án đã loại bỏ hoàn toàn backend cũ chạy trên Railway.app. Mọi logic lấy dữ liệu hiện tại đều chạy local trên thiết bị.
- Đảm bảo thiết bị của bạn có kết nối internet ổn định để lấy transcript từ YouTube và gọi Gemini API.

## 📄 Bản quyền

Copyright 2026 Nguyễn Duy Trường
