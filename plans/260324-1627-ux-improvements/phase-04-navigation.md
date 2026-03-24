# Phase 04: Sửa lỗi Swipe Back Navigation
Status: ⬜ Pending
Dependencies: Phase 03

## Objective
Chỉnh sửa sự cố Android Gesture / Android Back Button. Hiện tại nếu vuốt ở viền sẽ vô tình đóng cả App. Cần đổi lại để nó chỉ thoát màn hình `SummaryScreen` và quay lại `MainScreen`.

## Implementation Steps
1. [ ] Thêm `@Composable BackHandler` của thư viện Jetpack Compose.
2. [ ] Cấu hình BackHandler để gọi callback `onBack()` hoặc cập nhật state `currentScreen = "main"`.
3. [ ] Đảm bảo nút mũi tên ở Top App Bar cũng nhất quán với BackHandler này.

## Files to Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryScreen.kt`

---
Next Phase: (Done)
