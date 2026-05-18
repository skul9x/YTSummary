# YTSummary - Trình Tóm Tắt Video YouTube Bằng AI 🚀

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Badge"/>
  <img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Badge"/>
  <img src="https://img.shields.io/badge/Compose-Jetpack-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Compose Badge"/>
  <img src="https://img.shields.io/badge/AI-Gemini%20API-F4B400?style=for-the-badge&logo=google&logoColor=white" alt="Gemini Badge"/>
</div>

---

**YTSummary** là một ứng dụng Android hiện đại, mạnh mẽ giúp bạn tóm tắt nội dung của bất kỳ video YouTube nào một cách siêu tốc và thông minh bằng Trí Tuệ Nhân Tạo (Google Gemini AI). Giờ đây, bạn có thể dễ dàng nắm bắt nội dung cốt lõi của các video dài hàng giờ chỉ trong vài giây đọc bản tóm tắt súc tích ngay trên thiết bị điện thoại của mình.

---

## ✨ Các Tính Năng Nổi Bật

*   🎙️ **Trích Xuất Phụ Đề Siêu Tốc (Native Transcript Parser):** Lấy toàn bộ phụ đề video trực tiếp bằng Kotlin Native mà không cần phụ thuộc vào bất kỳ server backend trung gian nào.
*   🤖 **Tích Hợp Gemini AI Đa Dạng:** Hỗ trợ các dòng mô hình ngôn ngữ lớn mới nhất của Google (Gemini Flash, Gemini Pro) để mang lại bản tóm tắt chuẩn xác, cấu trúc mạch lạc.
*   🔄 **Xoay Tua API Key & Tự Phục Hồi (Smart Rotation & Self-Healing):** Cơ chế xoay vòng thông minh các API Key và Model để vượt qua giới hạn lượt gọi (Rate Limit - HTTP 429). Đặc biệt tích hợp cơ chế tự phục hồi (Self-Healing) ở cả tầng Database và UI để tự động khôi phục cấu hình mặc định an toàn khi dữ liệu trống, tránh tuyệt đối tình trạng crash runtime.
*   🔊 **Đọc Thành Tiếng Thông Minh (Smart TTS):** Đọc bản tóm tắt bằng giọng nói tự nhiên, tự động chia nhỏ các đoạn văn dài, hỗ trợ tự động dừng/phát khi có cuộc gọi đến.
*   📱 **Giao Diện Glassmorphism Cao Cấp:** Trải nghiệm thị giác đỉnh cao với phong cách thiết kế kính mờ hiện đại, mượt mà, hiệu ứng động tinh tế và tối ưu chế độ tối (Dark Mode).
*   💾 **Quản Lý Lịch Sử Tiện Lợi:** Lưu trữ, tìm kiếm và xem lại các bản tóm tắt cũ ngoại tuyến thông qua Room Database bảo mật cao.
*   ⚡ **Hiệu Năng Vượt Trội:** Ứng dụng khởi động tức thì và chạy mượt mà nhờ tối ưu hóa kích thước bằng R8 và thiết lập Baseline Profiles.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

*   **Ngôn ngữ lập trình:** Kotlin (1.9+)
*   **Giao diện người dùng (UI):** Jetpack Compose (Modern Declarative Toolkit)
*   **Kiến trúc hệ thống:** MVVM (Model-View-ViewModel) + Clean Architecture
*   **Cơ sở dữ liệu:** Room Persistence Library (SQLite wrapper)
*   **Kết nối mạng:** OkHttp3 & Retrofit (để gọi YouTube và Gemini API)
*   **Trí tuệ nhân tạo:** Google Gemini API Client
*   **Xử lý bất đồng bộ:** Kotlin Coroutines & Flow
*   **Tải hình ảnh:** Coil Image Loader (được cấu hình lưu bộ nhớ đệm tối ưu)
*   **Dependency Injection:** Manual Dependency Injection (Module-based DI)
*   **Hệ thống Build:** Gradle (Kotlin DSL)

---

## 📂 Cấu Trúc Thư Mục Dự Án

```text
app/src/main/java/com/skul9x/ytsummary/
├── api/            # Tương tác và kết nối API ngoài (Gemini & YouTube InnerTube)
├── data/           # Các Entity Room Database và tầng lưu trữ dữ liệu local
├── di/             # Module quản lý, cấu hình và khởi tạo các Dependency
├── manager/        # Bộ điều phối API Key, Quota, xoay tua Model và TTS Manager
├── model/          # Định nghĩa cấu trúc dữ liệu và Sealed Class trạng thái UI
├── repository/     # Cầu nối trung gian cung cấp dữ liệu giữa Local và Remote
├── ui/             # Các màn hình Compose (History, Summary, Settings) & ViewModels
└── util/           # Tiện ích chung (Clean văn bản, chia khối văn bản, tự động thử lại)
```

---

## 🚀 Hướng Dẫn Cài Đặt

### 1. Clone Source Code
Mở terminal trên máy tính của bạn và chạy lệnh sau:
```bash
git clone https://github.com/skul9x/YTSummary.git
```

### 2. Mở Dự Án Bằng Android Studio
*   Khởi động **Android Studio** (Khuyến nghị phiên bản **Ladybug 2024.2.1** trở lên để tương thích tối đa).
*   Chọn **Open** và dẫn tới thư mục `YTSummary` vừa clone về.
*   Đợi Gradle đồng bộ và tải các dependency cần thiết.

### 3. Thiết Lập API Key
*   Truy cập [Google AI Studio](https://aistudio.google.com/) để tạo một hoặc nhiều **Gemini API Key** hoàn toàn miễn phí.
*   Chạy ứng dụng lên thiết bị, đi tới màn hình **Cài Đặt** (Settings) -> Chọn **Quản Lý API Key** để thêm các khóa vừa tạo. Cơ chế xoay tua thông minh sẽ tự động phân phối tải giữa các khóa này!

---

## 📝 Hướng Dẫn Sử Dụng

1.  Mở ứng dụng **YouTube** trên điện thoại và mở video bạn muốn xem tóm tắt.
2.  Nhấn nút **Chia sẻ** (Share) dưới video -> Chọn biểu tượng **YTSummary** từ danh sách.
3.  Ứng dụng sẽ tự động khởi chạy, trích xuất phụ đề của video và bắt đầu quá trình tóm tắt thông minh bằng AI.
4.  Bạn có thể chọn đọc trực tiếp bản tóm tắt hoặc bấm biểu tượng **Loa phát thanh** để nghe đọc thành tiếng (TTS).

---

## ⚙️ Quy Trình CI/CD Tự Động

Dự án tích hợp hệ thống **GitHub Actions CI/CD** chuyên nghiệp được định nghĩa trong `.github/workflows/build.yml` giúp tự động hóa quá trình đóng gói và phát hành:
*   **Tự động chạy Unit Test:** Kiểm tra tính toàn vẹn và đảm bảo chất lượng mã nguồn trước khi đóng gói.
*   **Đóng gói APK ký số tự động:** Tự động tạo và sử dụng keystore tự ký an toàn để build ra tệp APK Release.
*   **Đầu ra chuẩn xác:** Đóng gói tệp APK với tên chính xác là `YTSummary-v1.0.8.apk`.
*   **Tự động tạo GitHub Release:** Sau khi build thành công, hệ thống tự động tạo một phiên bản Release mới trên GitHub với tag `v1.0.8` và đính kèm tệp `YTSummary-v1.0.8.apk` vào phần assets của Release đó.
*   **Kích hoạt linh hoạt:** Workflow tự động chạy bất cứ khi nào có thay đổi được push vào nhánh `main` hoặc khi có tag phiên bản mới được đẩy lên.

---

## 📄 Bản Quyền & Giấy Phép

> [!NOTE]
> Bản quyền phần mềm và mã nguồn thuộc về tác giả. Mọi hình thức tái cấu trúc hoặc sử dụng thương mại vui lòng ghi rõ nguồn và tuân thủ các điều khoản đóng góp mã nguồn mở.

Copyright {this_year} Nguyễn Duy Trường

---
*Phát triển và duy trì bởi Nguyễn Duy Trường với tất cả sự đam mê dành cho thế giới Mã Nguồn Mở.*
