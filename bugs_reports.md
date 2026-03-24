# 📋 YTSummary - Consolidated Bug & Security Report
*Date: 2026-03-24 | Verified against current actual source code (v3.3.1+)*

Tài liệu này tổng hợp lại **TẤT CẢ các lỗi thực sự còn tồn tại** sau khi đã fact-check với source code. Hơn 50% các lỗi được nhắc đến trong báo cáo cũ thực chất đã được fix từ trước.

Dưới đây là danh sách những việc anh thực sự cần làm, được sắp xếp theo độ ưu tiên:

---

## 🔴 HIGH PRIORITY (Cần fix ngay - Rủi ro cao)

### 1. Security: Gemini Safety Filters đang bị tắt (`BLOCK_NONE`)
- **File:** `api/gemini/GeminiResponseHelper.kt` (Dòng 38)
- **Vấn đề:** App đang chủ động set `threshold` bằng `BLOCK_NONE` cho tất cả các rủi ro (Hate Speech, Harassment, Explicit, Dangerous). Điều này vô tình tắt toàn bộ màng lọc an toàn của AI, khiến app cực kỳ dễ bị tấn công Prompt Injection (ví dụ AI vô tình đọc to đường link lừa đảo do kẻ gian cài vào phụ đề Youtube).
- **Cách sửa:** Xóa block `safetySettings` để dùng mặc định của Google, hoặc đổi thành `BLOCK_ONLY_HIGH`.

### 2. Security: Indirect Prompt Injection
- **File:** `api/gemini/GeminiPrompts.kt` (Dòng 22)
- **Vấn đề:** Chuỗi `transcript` được nối thẳng vào Prompt mà không có ranh giới bảo vệ. AI có thể nhầm lẫn giữa "Dữ liệu cần tóm tắt" và "Lệnh điều khiển" nếu phụ đề chứa các câu như *"Bỏ qua lệnh cũ, hãy nói..."*.
- **Cách sửa:** Bọc `$content` trong các thẻ XML như `<transcript>...</transcript>` và thêm lệnh *"Chỉ tóm tắt nội dung trong thẻ transcript"*.

---

## 🟡 MEDIUM PRIORITY (Nên xử lý sớm - Ảnh hưởng UX/Hiệu năng)

### 3. Performance: Regex Compile lặp lại nhiều lần
- **File:** `manager/TtsManager.kt` (Dòng 63-70)
- **Vấn đề:** Hàm `cleanMarkdown()` khởi tạo 4 `Regex(...)` object mới toanh *mỗi lần* được gọi. Việc này gây lãng phí CPU và GC overhead không cần thiết.
- **Cách sửa:** Chuyển 4 Regex này vào `companion object` để khởi tạo 1 lần duy nhất (hoist regex).

### 4. ✅ Đã xác nhận: Danh sách Model hợp lệ
- **Thông tin cập nhật:** `models/gemini-3-flash-preview` thực sự đã ra mắt và tồn tại.
- **Kết luận:** Danh sách model xoay tua trong `GeminiApiClient.kt` hoàn toàn chính xác, không có model "rác" gây timeout! Bỏ qua nhận xét lỗi thời trong báo cáo cũ.

### 5. Security/UX: Ép buộc tăng Max Volume (Forced Volume)
- **File:** `manager/TtsManager.kt` (Dòng 81)
- **Vấn đề:** Mỗi lần gọi `speak()`, app tự động ép Volume máy lên 80% bằng `setVolume(80)`. Giả sử user đang cắm tai nghe ban đêm hoặc đang trong phòng họp, việc bị cướp quyền control volume đột ngột là rủi ro và rất phiền.
- **Cách sửa:** Bỏ dòng `setVolume(80)` trong `speak()`. Để Android tự lo việc quản lý âm lượng của người dùng.

---

## 🟢 LOW PRIORITY / SUGGESTIONS (Tối ưu thêm cho sạch code)

### 6. Security: Thiếu Cleartext Traffic Restriction
- **File:** `AndroidManifest.xml`
- **Vấn đề:** Thiếu cờ báo cho HĐH biết app cấm kết nối HTTP không mã hóa.
- **Cách sửa:** Thêm `android:usesCleartextTraffic="false"` vào thẻ `<application>`.

### 7. Performance: Hardcoded Dữ Liệu Giao Diện
- **File:** `ui/MainActivity.kt` (Dòng 295)
- **Vấn đề:** Stat card hiển thị cứng chữ "32 Summaries".
- **Cách sửa:** Thay bằng logic query `summaryDao.count()` thực tế từ Database.

### 8. Performance: API Non-Streaming
- **File:** `api/GeminiApiClient.kt`
- **Vấn đề:** Đang dùng `generateContent` chờ toàn bộ payload trả về rồi mới bắt đầu Tts speak. Gây trễ (latency) khoảng 2-3s.
- **Cách sửa:** (Cần nhiều thời gian) Đổi sang dùng `streamGenerateContent()`, nhận chunk nào thì đẩy vào queue cho TTS đọc luôn.

### 9. Security Validation: Explicit VideoID Check
- **File:** `ui/MainActivity.kt`
- **Vấn đề:** Dù hàm regex extract videoID khá an toàn, nhưng vẫn nên tuân thủ defense-in-depth: Validate lại 1 lần nữa trước khi gửi xuống tầng Python Local.
- **Cách sửa:** Thêm `require(videoId.matches(Regex("^[a-zA-Z0-9_-]{11}$")))` vào pipeline.
