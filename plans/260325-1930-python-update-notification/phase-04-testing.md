# Phase 04: Testing
Status: ⬜ Pending
Dependencies: phase-03-integration.md

## Objective
Manual test tính năng thông báo và đảm bảo mọi thứ không lỗi khi request permissions.

## Test Criteria
- [ ] Gắng buộc `getLatestVersion` lớn hơn `getInstalledVersion` (Bằng cách hardcode `testVersion = "0.6.0"` trong `PythonUpdateChecker`).
- [ ] Kiểm tra Dialog xin quyền Notification ở Android 13+.
- [ ] Notification hiển thị khi mở app.
- [ ] Tab vào Notification -> Chuyển về App.
- [ ] Đóng app vào lại -> Notification không bị bắn lại cho cùng một version (logic anti-spam hoạt động).

## Implementation Steps
1. Thực hiện test trên thiết bị thật / Emulator API 33+.
2. Fix edge-cases nếu Notification Permission bị denied.

---
End of Plan.
