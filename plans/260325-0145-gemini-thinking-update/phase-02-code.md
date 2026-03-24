# Phase 02: Code Modification (OkHttp/GeminiApiClient)
Status: ⬜ Pending
Dependencies: Phase 01

## Objective
Thực hiện thay đổi trong mã nguồn Kotlin (đặc biệt là file `GeminiApiClient.kt`) để cập nhật model endpoint sang bản 2.5 và bổ sung cấu hình `thinkingConfig`.

## Requirements
### Functional
- [ ] Đổi tên endpoint từ `gemini-X.X-flash` thành `gemini-2.5-flash` (hoặc cấu hình tương đương).
- [ ] Chèn JSON section `thinkingConfig` với `thinkingBudget: 0` vào payload request gửi đi.
- [ ] (Tuỳ chọn) Đảm bảo việc dùng endpoint `v1beta` nếu tính năng `thinkingConfig` yêu cầu phải gọi API beta.

## Implementation Steps
1. [x] Mở file `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt`.
2. [x] Tìm vị trí đóng gói JSON payload chứa `contents`.
3. [x] Thêm object cấu hình `{"thinkingConfig": {"thinkingBudget": 0}}`.
4. [x] Cập nhật lại đường dẫn URL gọi tới model `gemini-2.5-flash`.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt` - [Update Model Name]
- `app/src/main/java/com/skul9x/ytsummary/api/gemini/GeminiResponseHelper.kt` - [Update Request Body]

## Test Criteria
- [x] App compile thành công.
- [ ] Log payload verify có chứa mục `thinkingConfig`.

---
Next Phase: Phase 03 - Testing & Verification
