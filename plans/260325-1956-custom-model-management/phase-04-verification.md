# Phase 04: Verification & Testing
Status: ⬜ Pending
Dependencies: Phase 01, 02, 03

## Objective
Viết unit test cho `ModelManager` và đảm bảo existing tests không bị break.

## Files to Create/Modify

#### [NEW] `app/src/test/java/com/skul9x/ytsummary/manager/ModelManagerTest.kt`

Unit test cho `ModelManager` sử dụng MockK (mock SharedPreferences):

```kotlin
class ModelManagerTest {
    // Test cases:
    // 1. getModels() trả DEFAULT_MODELS khi prefs rỗng
    // 2. addModel() thêm thành công
    // 3. addModel() từ chối model trùng
    // 4. addModel() từ chối model rỗng/blank
    // 5. removeModel() xóa đúng index
    // 6. removeModel() với index ngoài phạm vi → false
    // 7. moveUp() swap đúng vị trí
    // 8. moveUp(0) → false (đã ở đầu)
    // 9. moveDown() swap đúng vị trí
    // 10. moveDown(lastIndex) → false (đã ở cuối)
    // 11. resetToDefaults() khôi phục DEFAULT_MODELS
    // 12. getModels() trả DEFAULT_MODELS khi list lưu trữ rỗng
}
```

## Verification Steps

### Automated Tests
1. [ ] Viết `ModelManagerTest.kt` với 12 test cases trên
2. [ ] Chạy `./gradlew test` — tất cả tests PHẢI pass (bao gồm cả existing tests)

### Manual Verification (User thực hiện trên app)
1. [ ] Mở Settings → kiểm tra section "Model Priority" hiện ra với 4 model mặc định
2. [ ] Thêm model mới (ví dụ `models/gemini-2.0-flash`) → kiểm tra hiện trong list
3. [ ] Bấm ▲/▼ → kiểm tra thứ tự thay đổi
4. [ ] Bấm 🗑 xóa model → kiểm tra model biến mất
5. [ ] Bấm Test → kiểm tra ✅/❌ hiện đúng
6. [ ] Bấm Reset Default → kiểm tra 4 model mặc định xuất hiện lại
7. [ ] Thoát app → mở lại Settings → kiểm tra thứ tự đã lưu đúng
8. [ ] Thử summarize video → kiểm tra app dùng model theo thứ tự mới

---
End of Plan
