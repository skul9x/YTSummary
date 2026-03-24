# YTSummary - YouTube Video Summarizer

YTSummary là một ứng dụng Android thông minh giúp bạn tóm tắt nội dung video YouTube một cách tự động và cực kỳ nhanh chóng. Thay vì mất hàng giờ để xem toàn bộ video, ứng dụng sẽ cung cấp cho bạn một bản tóm tắt nội dung ngắn gọn, chính xác bằng cách kết hợp thông minh giữa khả năng lấy phụ đề (transcript) tự động của YouTube và trí tuệ nhân tạo (AI) Gemini 2.5 Flash mạnh mẽ của Google.

## ✨ Tính Năng Nổi Bật

- **Tóm tắt bằng AI (Google Gemini 2.5 Flash):** Khả năng phân tích và rút gọn phụ đề tinh tế, cung cấp thông tin cốt lõi nhất.
- **Hoạt Động Hoàn Toàn Standalone (Local):** Được trang bị engine **Chaquopy** (Python 3.12 tích hợp trực tiếp trên Android), ứng dụng có thể lấy phụ đề YouTube một cách tự động thông qua chính IP thiết bị của bạn, giúp bypass hoàn toàn các hệ thống chống Bot chặn IP của YouTube.
- **Giao Diện Material 3 Hiện Đại:** Tối giản, thanh lịch, tốc độ phản hồi tính bằng mili-giây.

## ⚙️ Hướng Dẫn Cài Đặt

### 1. Yêu Cầu Hệ Thống
- **Android Studio:** Bản Hedgehog hoặc mới nhất.
- **Gradle:** Phiên bản được định nghĩa trong `wrapper`.
- **JDK:** Java 11 hoặc Java 17+.

### 2. Các Bước Cài Đặt
1. **Clone repository này về máy:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Mở dự án trong Android Studio.**
3. **Đồng bộ hóa (Sync):** Ấn `Sync Project with Gradle Files` và chờ Gradle tự động tải bộ công cụ (kèm Python runtime của Chaquopy).
4. **Nhập Google Gemini API Key:** Sau khi build chương trình thành công và mở App, hãy nhấn vào **Cài đặt (Settings)** (biểu tượng bánh răng) trên góc màn hình và paste đoạn API Key Gemini của bạn vào ô cung cấp (Dạng: `AIza...`).

## 🚀 Cách Sử Dụng

1. Mở ứng dụng YouTube gốc (hoặc thông qua trình duyệt).
2. Tìm video bạn muốn tóm tắt.
3. Nhấn nút **Chia sẻ (Share)** và chọn **Share to YTSummary**, hoặc bạn có thể copy link URL video dán vào giao diện chính của ứng dụng.
4. Chờ 1-3 giây để ứng dụng kéo phụ đề và trả về bản ghi tóm tắt cho bạn!

## 📂 Cấu Trúc Thư Mục

- `app/src/main/java/` - Chứa toàn bộ logic UI (Jetpack Compose) và kiến trúc chính của App (Kotlin).
   - `ui/` - Các màn hình giao diện cấu thành ứng dụng (Home, History, Summary, Settings...).
   - `manager/` - Quản lý Cấu hình và Logic cốt lõi (Giao tiếp với Python Engine, API Key Manager...).
- `app/src/main/python/` - Chứa mã nguồn Python `yt_transcript_helper.py` đảm nhận vai trò là Backend giả lập để scrap phụ đề tự động bằng thư viện `youtube-transcript-api`.
- `app/build.gradle.kts` - Manifest file build của dự án (Khai báo module Chaquopy 17.0).
- `.brain/` - Bộ nhớ AI và lưu trữ ngữ cảnh đặc tả kỹ thuật (Technical Contexts) của hệ thống AWF.

## 🤝 Thông Tin Bổ Sung
- Dự án ưu tiên sử dụng thiết kế clean architecture cơ bản trên điện thoại để loại bỏ hoàn toàn hệ thống Cloud Backend đắt đỏ.
- Do tích hợp Chaquopy để chạy bộ thư viện Python cục bộ, dung lượng APK build sẽ tăng thêm khoảng chừng ~25MB so với các phiên bản build Android Studio thông thường. Nhưng bù lại ứng dụng của bạn sẽ được an toàn khỏi IP Blocking!

---
**Bản quyền:**
Copyright 2026 Nguyễn Duy Trường
