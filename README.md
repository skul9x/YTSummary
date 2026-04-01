# 📺 YT Summary AI

**YT Summary AI** là một ứng dụng Android hiện đại được thiết kế để giúp người dùng tiết kiệm thời gian bằng cách tóm tắt các video YouTube dài thành các đoạn nội dung ngắn gọn và ý nghĩa thông qua sức mạnh của trí tuệ nhân tạo (Gemini AI).

---

## ✨ Tính năng nổi bật

- **Tóm tắt video tức thì (Streaming):** Hỗ trợ nhận kết quả từng phần từ Gemini API, giúp bạn xem nội dung tóm tắt ngay lập tức mà không cần chờ đợi.
- **Hỗ trợ giọng nói (TTS):** Tích hợp trình trợ lý giọng nói (Text-to-Speech) giúp bạn nghe bản tóm tắt, hỗ trợ đọc theo từng đoạn văn bản.
- **Điều khiển âm lượng thông minh:** Nút chuyển đổi âm lượng nhanh (80% - 85% - 90%) ngay trên giao diện AI Analysis.
- **Quản lý lịch sử (Offline):** Tự động lưu lại các bản tóm tắt đã xem cùng với ảnh thumbnail và tiêu đề video. Sử dụng Paging 3 để hiển thị danh sách mượt mà.
- **Giao diện hiện đại (Vibe Coding):** Thiết kế theo phong cách Neon Glassmorphism, mang lại trải nghiệm người dùng cao cấp và chuyên nghiệp.
- **Tối ưu hiệu năng:** Áp dụng Baseline Profiles để giảm độ trễ khi khởi động ứng dụng và tăng tính mượt mà cho UI.

---

## 🛠️ Công nghệ sử dụng

Dự án được xây dựng dựa trên các công nghệ tiên tiến nhất:

- **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/) (2.0+)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **AI Engine:** [Google Gemini API](https://ai.google.dev/) (mô hình Gemini 1.5 Flash & Pro)
- **Cơ sở dữ liệu:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) & [SQLCipher](https://www.zetetic.net/sqlcipher/) (Bảo mật dữ liệu)
- **Mạng:** [OkHttp](https://square.github.io/okhttp/) (Hỗ trợ SSE streaming)
- **Hình ảnh:** [Coil](https://coil-kt.github.io/coil/compose/)
- **Kiến trúc:** MVVM (Model-View-ViewModel) kết hợp Repository Pattern
- **Performance:** [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles/overview)

---

## 🚀 Hướng dẫn cài đặt

1. **Clone dự án:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Cấu hình API Key:**
   - Đăng ký API Key tại [Google AI Studio](https://aistudio.google.com/).
   - Nhập API Key vào mục **Settings** trong ứng dụng.
3. **Build & Chạy:**
   - Mở dự án bằng Android Studio.
   - Nhấn nút **Run** để cài đặt lên thiết bị (Yêu cầu API 26+).

---

## 📂 Cấu trúc thư mục chính

```text
app/src/main/java/com/skul9x/ytsummary/
├── api/             # Giao tiếp với Gemini AI (Streaming/Rotation)
├── data/            # Room Database, Entities, Daos
├── manager/         # TtsManager, ApiKeyManager, ModelManager
├── model/           # Data classes cho AI Result/UI state
├── repository/      # SummarizationRepository xử lý logic chính
├── ui/              # Compose Screens & ViewModels
└── util/            # Helper classes (SummaryUtils, RetryUtils)
```

---

## ⚖️ Bản quyền

Copyright 2026 Nguyễn Duy Trường

*Phát triển bởi Nguyễn Duy Trường - Skul9x.*
