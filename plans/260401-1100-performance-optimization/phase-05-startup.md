# Phase 05: Startup deferral and frame-metric verification
Status: ✅ Completed
Dependencies: None

## Objective
Tối ưu hóa thời điểm khởi tạo tài nguyên và thay đổi cách xử lý TTS cho văn bản dài để bảo vệ luồng chính (Main Thread) và giảm áp lực lên hệ thống.

## Requirements
### Functional
- [x] Dời khởi tạo `TtsManager` ra sau frame đầu tiên.
- [x] Chuyển `TtsManager.speakInChunks` từ đệ quy sang lặp (Iterative).
- [x] Trì hoãn NotificationChannel init.

### Non-Functional
- [ ] Tốc độ mở màn hình (`onCreate` -> `onStart`) cải thiện > 30%.
- [ ] Tuyệt đối không xảy ra StackOverflowError khi văn bản tóm tắt siêu dài.

## Implementation Steps
1. [x] Sửa `MainActivity.kt`: Sử dụng `View.post` hoặc Coroutine delay để dời init.
2. [x] Sửa `TtsManager.kt`: Refactor hàm đệ quy sang `while` / `Queue`.
3. [x] Verifier bằng `StartupTimingMetric`.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/MainActivity.kt`
- `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt`

## Test Criteria
- [ ] Test tóm tắt video > 1 giờ (siêu dài).
- [ ] Kiểm tra Trace Startup bằng Profiler.
- [ ] Verify Audio Focus vẫn hoạt động khi chuyển phase.
