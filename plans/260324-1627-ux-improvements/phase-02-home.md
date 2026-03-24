# Phase 02: Nút Paste & Tóm tắt nhanh (Home)
Status: ⬜ Pending
Dependencies: Phase 01

## Objective
Cải thiện đáng kể luồng thao tác sử dụng ở màn hình chính. Người dùng không cần bấm vào ô điền chữ rồi Paste thủ công, sau đó mới bấm nút mũi tên nữa. Một nút duy nhất "Paste & Tóm tắt" sẽ lo liệu mọi thứ.

## Implementation Steps
1. [ ] Cập nhật giao diện màn hình chính (`MainScreen` hoặc component tương ứng trong `MainActivity.kt`).
2. [ ] Thêm nút to, rõ ràng "Paste & Tóm tắt" bên dưới thanh điền link youtube.
3. [ ] Bắt sự kiện tạo `ClipboardManager`, lấy text từ clipboard mới nhất.
4. [ ] Validate nếu text hợp lệ thì đính vào State Text Field, tự động trigger event click Start.

## Files to Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/MainActivity.kt`

---
Next Phase: phase-03-analysis.md
