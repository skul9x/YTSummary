# Phase 01: ModelManager (Data Layer)
Status: ⬜ Pending
Dependencies: None

## Objective
Tạo class `ModelManager` để lưu/đọc/sắp xếp danh sách Model từ SharedPreferences, thay thế `MODELS` hard-code trong `GeminiApiClient`.

## Files to Create/Modify

#### [NEW] `app/src/main/java/com/skul9x/ytsummary/manager/ModelManager.kt`

Class singleton (giống pattern `ApiKeyManager`) với các chức năng:

```kotlin
class ModelManager private constructor(context: Context) {
    companion object {
        // Default models (giữ nguyên thứ tự hiện tại từ GeminiApiClient.MODELS)
        val DEFAULT_MODELS = listOf(
            "models/gemini-3.1-flash-lite-preview",
            "models/gemini-3-flash-preview",
            "models/gemini-2.5-flash-lite",
            "models/gemini-2.5-flash"
        )
        
        // Singleton getInstance()
    }
    
    // SharedPreferences (thường, không cần encrypt vì tên model không nhạy cảm)
    private val prefs: SharedPreferences
    
    fun getModels(): List<String>       // Trả về danh sách model. Nếu rỗng → trả DEFAULT_MODELS
    fun saveModels(models: List<String>) // Lưu danh sách
    fun addModel(modelName: String): Boolean  // Thêm (từ chối nếu trùng hoặc rỗng)
    fun removeModel(index: Int): Boolean      // Xóa theo index
    fun moveUp(index: Int): Boolean           // Swap với item trên
    fun moveDown(index: Int): Boolean         // Swap với item dưới
    fun resetToDefaults()                     // Xóa tất cả → trả về DEFAULT_MODELS
    fun isEmpty(): Boolean                    // Kiểm tra SharedPreferences có data chưa
}
```

**Logic quan trọng:**
- `getModels()` nếu SharedPreferences rỗng (user mới / chưa config) → trả `DEFAULT_MODELS`
- `getModels()` nếu SharedPreferences có data nhưng list rỗng (user xóa hết) → trả `DEFAULT_MODELS`
- Serialization: dùng `kotlinx.serialization.json.Json` (đã có trong project)

## Implementation Steps
1. [ ] Tạo file `ModelManager.kt` trong package `manager`
2. [ ] Implement singleton pattern (companion object + volatile instance)
3. [ ] Implement CRUD operations (get/add/remove/moveUp/moveDown/resetToDefaults)
4. [ ] Implement fallback logic: list rỗng → DEFAULT_MODELS

## Test Criteria
- [ ] `getModels()` trả về DEFAULT_MODELS khi chưa có data
- [ ] `addModel()` thêm thành công và từ chối model trùng
- [ ] `removeModel()` xóa đúng index
- [ ] `moveUp()` / `moveDown()` swap đúng vị trí
- [ ] `resetToDefaults()` khôi phục lại DEFAULT_MODELS
- [ ] List rỗng sau khi xóa hết → tự fallback về DEFAULT_MODELS

---
Next Phase: → phase-02-api-integration.md
