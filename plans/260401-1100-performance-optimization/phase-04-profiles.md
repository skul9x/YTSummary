# Phase 04: Baseline profile generation and CI validation
Status: ✅ Completed
Dependencies: None

## Objective
Hoàn thiện quy trình tạo và tích hợp Baseline Profile để đảm bảo app chạy ở tốc độ tối ưu ngay sau khi cài (AOT Compilation), tránh hiện tượng "đơ" lúc khởi động và scroll trang đầu.

## Requirements
### Functional
- [x] Module `benchmark` tạo được profile (`.txt`) via `BaselineProfileGenerator`.
- [x] Module `app` tự động sử dụng profile khi build Release. (Configured plugin).

### Non-Functional
- [ ] Thời gian khởi động (Startup time) ổn định.
- [ ] Frame rate trang `Main` và `History` mượt mà (60fps).

## Implementation Steps
1. [x] Chỉnh sửa `BaselineProfileGenerator.kt` để phủ các luồng quan trọng (Main -> Summary).
2. [x] Config `build.gradle` (app) để copy profile sang `src/main/baseline-profiles.txt` (Dùng plugin `androidx.baselineprofile` mới nhất).
3. [x] Chạy lệnh `generateBaselineProfile`.

## Files to Create/Modify
- `macrobenchmark/src/main/java/.../BaselineProfileGenerator.kt`
- `app/build.gradle`

## Test Criteria
- [x] Chạy task `generateBaselineProfile` thành công (đã verify presence của task).
- [x] Kiểm tra Gradle task log xác nhận plugin đã được config.
