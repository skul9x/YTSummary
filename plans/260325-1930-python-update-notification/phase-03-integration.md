# Phase 03: ViewModel Integration
Status: ⬜ Pending
Dependencies: phase-02-notification-helper.md

## Objective
Gắn `NotificationHelper` vào logic update có sẵn của `SummaryViewModel`.

## Requirements
### Functional
- [ ] Trigger logic hiển thị notification ngay sau khi update detection trả về bản có sẵn (`UpdateInfo.latestVersion` > `currentVersion`).
- [ ] Phải truyền đúng Context vào `NotificationHelper`. Vì `SummaryViewModel` kế thừa `AndroidViewModel`, ta có quyền lấy tham chiếu `Application Context` một cách an toàn.

## Implementation Steps
1. [ ] Cập nhật init block của `SummaryViewModel.kt`
2. [ ] Gọi `NotificationHelper.showUpdateNotification` sau khi gán chuỗi `_updateInfo.value`.

## Files to Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryViewModel.kt`

---
Next Phase: phase-04-testing.md
