Status: ✅ Complete
Dependencies: Phase 02

## Objective
Porting cơ chế xoay tua API Key từ dự án RSS-Reader sang dự án mới. Đảm bảo app có thể tự động đổi Key khi gặp giới hạn 429.

## Requirements
### Functional
- [ ] `ApiKeyManager`: Lưu và lấy danh sách Keys từ bộ nhớ an toàn.
- [ ] `ModelQuotaManager`: Đánh dấu Key bị "cấm" khi gặp lỗi quota.
- [ ] `GeminiService`: Gửi Prompt tóm tắt kèm Transcript lên Google AI Studio.
- [ ] Logic Retry: Tự động thử lại với Key khác nếu Key hiện tại lỗi.

## Implementation Steps
1. [x] Clone `ApiKeyManager.kt` và `ModelQuotaManager.kt` từ `RSS-Reader-main`.
2. [x] Chỉnh sửa logic `ModelQuotaManager` để phù hợp với Gemini 2.0 Flash.
3. [x] Cấu hình `EncryptedSharedPreferences` để lưu danh sách Key.
4. [x] Viết `AILogicOrchestrator` để kết nối: Lấy Key -> Gọi Gemini -> Thành công hoặc Đổi Key.

## Files to Create/Modify
- `app/src/.../manager/ApiKeyManager.kt` - Ported.
- `app/src/.../manager/ModelQuotaManager.kt` - Ported.
- `app/src/.../api/GeminiApiClient.kt` - Gọi API trực tiếp của Google.

## Test Criteria
- [ ] Nhập 3 Keys (1 lỗi, 2 sống) -> App phải tự động chuyển sang Key thứ 2 để tóm tắt thành công.
- [ ] Logcat hiển thị đúng quá trình chuyển đổi Key.

---
Next Phase: [Phase 04: Frontend UI & Experience](phase-04-frontend-ui.md)
