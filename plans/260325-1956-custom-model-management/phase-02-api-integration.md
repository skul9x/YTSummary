# Phase 02: GeminiApiClient Integration
Status: ⬜ Pending
Dependencies: Phase 01 (ModelManager)

## Objective
Sửa `GeminiApiClient` để đọc danh sách model từ `ModelManager` thay vì hard-code `MODELS`. Đảm bảo `SummarizationRepository` inject `ModelManager` đúng cách.

## Files to Modify

#### [MODIFY] `GeminiApiClient.kt`

**Thay đổi chi tiết:**

1. **Constructor:** Thêm `ModelManager` vào constructor parameter:
```diff
 class GeminiApiClient(
     private val apiKeyManager: ApiKeyManager,
     private val quotaManager: ModelQuotaManager,
+    private val modelManager: ModelManager,
     private val client: OkHttpClient = NetworkModule.okHttpClient,
     private val baseUrl: String = BASE_URL
 )
```

2. **`summarize()` function:** Thay `MODELS` bằng `modelManager.getModels()`:
```diff
-for (model in MODELS) {
+for (model in modelManager.getModels()) {
```

3. **Companion object:** Giữ `DEFAULT_MODELS` trong `ModelManager` (đã có ở Phase 01). Xóa `val MODELS` khỏi companion object, chỉ giữ `TAG` và `BASE_URL`.

---

#### [MODIFY] `SummarizationRepository.kt`

**Thay đổi:** Thêm `ModelManager` vào khi tạo `GeminiApiClient`:
```diff
 private val geminiApi = GeminiApiClient(
     apiKeyManager = ApiKeyManager.getInstance(context),
-    quotaManager = ModelQuotaManager.getInstance(context)
+    quotaManager = ModelQuotaManager.getInstance(context),
+    modelManager = ModelManager.getInstance(context)
 )
```

## Implementation Steps
1. [ ] Thêm `ModelManager` vào constructor `GeminiApiClient`
2. [ ] Thay `MODELS` → `modelManager.getModels()` trong `summarize()`
3. [ ] Xóa `val MODELS` khỏi companion object (chuyển sang `ModelManager.DEFAULT_MODELS`)
4. [ ] Cập nhật `SummarizationRepository` inject `ModelManager`
5. [ ] Kiểm tra không còn reference nào đến `GeminiApiClient.MODELS` trong codebase

## Test Criteria
- [ ] `GeminiApiClient` nhận model list từ `ModelManager` thay vì hard-code
- [ ] Build thành công không lỗi compilation
- [ ] `./gradlew test` pass (existing tests không bị break)

---
Next Phase: → phase-03-settings-ui.md
