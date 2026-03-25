# Phase 02: Notification Helper
Status: ⬜ Pending
Dependencies: phase-01-setup.md

## Objective
Tạo module trợ giúp xây dựng và hiển thị System Notification để gom nhóm logic, tránh rác `MainActivity`.

## Requirements
### Functional
- [ ] Tạo `NotificationHelper` object.
- [ ] Có hàm `createChannel` để khởi tạo channel.
- [ ] Có hàm `showUpdateNotification` để bắn Notification.
- [ ] **Anti-spam logic**: Dùng SharedPreferences để lưu version đã notify, nếu notify rồi thì không bắn lại dù vẫn còn detect thấy update mới để tránh phiền User.
- [ ] Tích hợp `PendingIntent` mở lại app.

## Implementation Steps
1. [ ] Tạo file `NotificationHelper.kt` trong package `manager`.
2. [ ] Viết hàm `createChannel()`.
3. [ ] Viết hàm `showUpdateNotification(context, currentVersion, latestVersion)`.

## Files to Create
- `app/src/main/java/com/skul9x/ytsummary/manager/NotificationHelper.kt`

---
Next Phase: phase-03-integration.md
