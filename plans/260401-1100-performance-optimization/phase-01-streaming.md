# Phase 01: Streaming delta model + UI cadence throttling
Status: ✅ Completed
Dependencies: None

## Objective
Loại bỏ hành vi "O(n^2)" khi emit toàn bộ text mỗi khi có chunk mới. Chuyển sang mô hình "Append-only" và giới hạn tần suất cập nhật UI.


## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/model/AiResult.kt`
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt`
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryViewModel.kt`
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryScreen.kt`

## Test Criteria
- [ ] Test bằng video > 20 phút (nhiều text).
- [ ] Kiểm tra RAM khi đang stream không tăng vọt.
- [ ] Kiểm tra tính toàn vẹn của text cuối cùng (Final text matching).
