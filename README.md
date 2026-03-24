# YTSummary

## Tổng quan Dự án
YTSummary là một ứng dụng Android thông minh, đóng vai trò như một trợ lý AI có khả năng tóm tắt video YouTube một cách thần tốc trực tiếp trên điện thoại của bạn! 

Ứng dụng được thiết kế bảo mật chặt chẽ, tối ưu bằng công nghệ song song: Vừa tải phụ đề video một cách cục bộ với tốc độ cao (thông qua Chaquopy) mà không bị giới hạn bởi nhà mạng, vừa phân tích qua Gemini AI với chế độ Stream thời gian thực, cho bạn kết quả mượt mà và sâu sắc.

## Tính năng Chính
* **Trích xuất phụ đề cực nhanh:** Lấy dữ liệu Transcript cục bộ thông qua nền tảng Python (Chaquopy) được tích hợp trực tiếp thay vì phụ thuộc qua máy chủ trung gian. Hỗ trợ phụ đề Tiếng Việt và Tiếng Anh.
* **Tóm tắt bằng Gemini 2.5 Flash:** Sử dụng mô hình AI Gemini mới nhất giúp mang lại thông tin đầy đủ, cô đọng.
* **Giao diện Streaming Thời Gian Thực:** Hiệu ứng chữ chạy mượt mà theo giao thức SSE, đảm bảo trải nghiệm tức thì ngay khi AI bắt đầu phân tích.
* **Quyền riêng tư tuyệt đối:** Dữ liệu hệ thống, giao dịch khóa API được mã hóa vĩnh viễn với Room Database + SQLCipher, giúp chống rò rỉ kể cả khi máy bạn có bị xâm nhập. Lọc API Key tự động nhận diện nhờ Regex thông minh.
* **Đọc Tự Động (Auto-TTS) x AudioFocus:** Chạy nền AI đọc tóm tắt ngay sau khi hoàn thành. Hệ thống tuân chuẩn cấu hình âm thanh tự hạ nền nhạc đang nghe (Ducking) giúp bạn không bị gián đoạn giải trí.
* **Kết hợp Share Intent (1-Click):** Khi xem YouTube, bấm Chia Sẻ thẳng vào YTSummary, ứng dụng sẽ trích xuất ID video để tóm tắt ngay lập tức.

## Hướng dẫn Cài đặt
1. Tải git nguyên bản của dự án:
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. Cài đặt **Android Studio** phiên bản mới nhất hỗ trợ Kotlin và JDK 17+. Lần khởi động đầu tiên máy sẽ cập nhật thư viện Python cho hệ thống Chaquopy.
3. Khi bạn khởi chạy qua máy ảo Emulator hoặc máy thật: Lúc này có thể ứng dụng yêu cầu cấp **API Key Gemini** của Google AI Studio. 
4. Truy cập Google AI Studio để lấy API, dán trực tiếp trên Cài đặt của Ứng dụng. 

## Cấu trúc Thư mục
Dự án được phân cấp nhằm hỗ trợ tốt nhất cho luồng làm tác vụ song song:
```text
YTSummary/
├── app/
│   ├── src/main/java/              # Code Kotlin + Jetpack Compose (Luồng Android)
│   ├── src/main/python/            # Nơi chứa kịch bản Python tải phụ đề
│   ├── src/main/res/               # Tài nguyên (Adaptive Launcher Vector)
├── docs/                           # Tài nguyên phân tích kiến trúc, lỗi bug, System Overview.
├── .brain/                         # Thư mục "Hộp Ký Ức" của AI/Antigravity giúp theo dõi liên tục dòng thời gian sửa lỗi dự án (Bao gồm Session lưu trữ và kiến trúc tĩnh).
├── README.md                       # Tài liệu hướng dẫn bạn đang đọc.
```

## Thông tin Bổ sung
**Gemini 2.5 Migration Update:** Phiên bản hiện tại đã loại bỏ cấu hình `thinkingConfig` nhằm cải thiện thời gian phản hồi (TTFB).

## Bản quyền
Copyright 2026 Nguyễn Duy Trường
