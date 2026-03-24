# Phase 01: Cải tiến Settings UI (API Keys)
Status: ⬜ Pending

## Objective
Thay vì phải nhập thủ công từng API Key, tính năng này cho phép dán nguyên một đống dữ liệu lộn xộn có chứa API Key. App tự tìm, bóc tách và lưu các API Key hơp lệ.

## Implementation Steps
1. [ ] Sửa `OutlinedTextField` nhập Key ở file `SettingsScreen.kt` thành dạng Multi-line (area).
2. [ ] Thêm nút button "Paste & Lọc API Từ Clipboard".
3. [ ] Code logic Regex hoặc Split String bằng khoảng trắng/dấu phẩy để nhận diện các chuỗi bắt đầu với `AIza`.
4. [ ] Duyệt danh sách mới và update vào CSDL (`ApiKeyManager` hoặc Room DB).

## Files to Modify
- `app/src/main/java/com/skul9x/ytsummary/ui/SettingsScreen.kt`

---
Next Phase: phase-02-home.md
