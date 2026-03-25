# Phase 01: Permissions & Channel Setup
Status: ⬜ Pending
Dependencies: None

## Objective
Xin quyền hiển thị thông báo trên Android 13+ và khởi tạo Notification Channel.

## Requirements
### Functional
- [ ] Khai báo quyền `POST_NOTIFICATIONS` trong `AndroidManifest.xml`
- [ ] Thiết lập logic xin quyền ở `MainActivity` (Composition Local / Accompanist hoặc API chuẩn Jetpack Compose).
- [ ] Khởi tạo Notification Channel trong `Application` hoặc `MainActivity.onCreate()` với mức độ ưu tiên DEFAULT.

### Non-Functional
- [ ] Security: Xin quyền minh bạch, không crash app nếu bị từ chối.

## Implementation Steps
1. [ ] Cập nhật `AndroidManifest.xml`
2. [ ] Thêm logic xin quyền runtime trong `MainActivity` (Dùng Jetpack Compose Permissions API hoặc standard `registerForActivityResult`).

## Files to Modify
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/skul9x/ytsummary/ui/MainActivity.kt`

---
Next Phase: phase-02-notification-helper.md
