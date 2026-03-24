# Phase 03: Loading UX & Nút Copy Tóm tắt
Status: ⬜ Pending
Dependencies: Phase 02

## Objective
Trong thời gian app chờ gọi Python local, tải sub hoặc gọi Gemini, màn hình Analysis (Summary) cần hiển thị rõ ràng cho user biết app đang làm việc với chữ "Đang tóm tắt...". Khi chạy xong, kết quả cần được copy nhanh gọn.

## Implementation Steps
1. [ ] Xác định State Loading thay vì chỉ quay vòng tròn, phải thêm Chữ dưới Spinner báo là "Đang tóm tắt...".
2. [ ] Ở màn hình Result, phần sau khi chữ AI trả về, thêm chức năng Copy vào Clipboard (Dùng Icon Button dọn gàng góc phải).
3. [ ] Báo cho User UI bằng Toast nhỏ: "Đã copy vào bộ nhớ tạm".

## Files to Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryScreen.kt`

---
Next Phase: phase-04-navigation.md
