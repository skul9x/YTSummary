# 📺 YT Summary AI

**YT Summary AI** là một ứng dụng Android hiện đại được thiết kế để giúp người dùng tiết kiệm thời gian bằng cách tóm tắt các video YouTube dài thành các đoạn nội dung ngắn gọn và ý nghĩa thông qua sức mạnh của trí tuệ nhân tạo (Gemini AI).

---

## ✨ Tính năng nổi bật

- **Tóm tắt video tức thì:** Hỗ trợ streaming kết quả từ Gemini API, giúp bạn xem nội dung tóm tắt ngay khi nó đang được tạo ra.
- **Hỗ trợ giọng nói (TTS):** Tích hợp trình trợ lý giọng nói giúp bạn nghe bản tóm tắt mà không cần nhìn vào màn hình.
- **Điều khiển âm lượng nhanh:** Nút chuyển đổi âm lượng thông minh (80% - 85% - 90%) ngay trên giao diện tóm tắt.
- **Lưu lịch sử:** Tự động lưu lại các bản tóm tắt đã xem cùng với ảnh thumbnail và tiêu đề video để xem lại offline.
- **Giao diện hiện đại:** Thiết kế theo phong cách Neon Glassmorphism cực kỳ bắt mắt và mượt mà.
- **Chia sẻ tóm tắt:** Dễ dàng sao chép nội dung tóm tắt để gửi cho bạn bè.

---

## 🛠️ Công nghệ sử dụng

Dự án được xây dựng dựa trên các công nghệ tiên tiến nhất của hệ sinh thái Android:

- **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Khai báo giao diện hiện đại)
- **AI Engine:** [Google Gemini API](https://ai.google.dev/)
- **Cơ sở dữ liệu:** [Room Persistence Library](https://developer.android.com/training/data-storage/room) (Quản lý lịch sử offline)
- **Mạng:** [OkHttp](https://square.github.io/okhttp/) & [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- **Hình ảnh:** [Coil](https://coil-kt.github.io/coil/compose/)
- **Kiến trúc:** Clean Architecture & MVVM (ViewModel, Repository, Data Sources)

---

## 🚀 Hướng dẫn cài đặt

1. **Clone dự án:**
   ```bash
   git clone https://github.com/skul9x/YTSummary.git
   ```
2. **Cấu hình API Key:**
   - Đăng ký API Key tại [Google AI Studio](https://aistudio.google.com/).
   - Mở dự án trong Android Studio.
   - (Thông thường sẽ cần file secret hoặc biến môi trường cho API Key).
3. **Build & Chạy:**
   - Nhấn nút **Run** (màu xanh) trong Android Studio để cài đặt lên thiết bị hoặc máy ảo.

---

## 📂 Cấu trúc thư mục chính

```text
app/src/main/java/com/skul9x/ytsummary/
├── api/             # Giao tiếp với Gemini API
├── data/            # Cấu trúc Room Database & Dao
├── di/              # Cấu hình Dependeny Injection (nếu có)
├── manager/         # Quản lý TTS, Notifications, v.v.
├── repository/      # Tầng trung gian quản lý dữ liệu
├── ui/              # Giao diện người dùng (Compose Screens, ViewModel)
└── util/            # Các hàm hỗ trợ (SummaryUtils, etc.)
```

---

## 📖 Cách sử dụng

1. **Từ app YouTube:** Nhấn nút **Chia sẻ** (Share) -> Chọn **YT Summary AI**.
2. **Dán Link trực tiếp:** Copy link YouTube, mở ứng dụng và nhấn nút **Paste & Tóm tắt nhanh**.
3. **Màn hình AI Analysis:** Đọc tóm tắt, nghe giọng nói hoặc điều chỉnh âm lượng nhanh ở góc trên bên phải.

---

## ⚖️ Bản quyền

Copyright 2026 Nguyễn Duy Trường

*Phát triển bởi đội ngũ đam mê công nghệ tại Skul9x.*
