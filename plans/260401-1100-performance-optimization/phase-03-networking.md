# Phase 03: Network response lifecycle hardening
Status: ✅ Completed
Dependencies: None

## Objective
Đảm bảo toàn bộ tài nguyên OkHttp (Response / Body) được giải phóng kịp thời, ngăn rò rỉ Socket và Memory, đặc biệt là khi có lỗi mạng.

## Requirements
### Functional
- [x] Mọi cuộc gọi `execute()` của OkHttpClient phải được bao bọc bởi `use { ... }`.
- [x] Không có exception nào làm bỏ qua việc gọi `close()`.

### Non-Functional
- [x] Số lượng Socket mở khi app chạy ổn định, không tăng vọt (Leak).
- [x] App ổn định khi nén nút tóm tắt liên tục (Stress test).

## Implementation Steps
1. [x] Sửa `YouTubeTranscriptService.kt`: Thêm `.use` bao bọc lấy `execute()`.
2. [x] Sửa `InnerTubeClient.kt`: Thêm `.use` bao bọc lấy `execute()`.
3. [x] Trace lại `GeminiApiClient.kt` để đảm bảo `.use` đã bao phủ toàn diện.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/transcript/YouTubeTranscriptService.kt`
- `app/src/main/java/com/skul9x/ytsummary/transcript/InnerTubeClient.kt`
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt`

## Test Criteria
- [x] Trace Socket qua Android Profiler khi chạy app nhiều lần.
- [x] Chạy với Wifi yếu / Sim bị chặn để đảm bảo lỗi 429 vẫn giải phóng tài nguyên.
