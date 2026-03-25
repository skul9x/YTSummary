# Phase 03: Settings UI
Status: ⬜ Pending
Dependencies: Phase 01 (ModelManager), Phase 02 (Integration)

## Objective
Thêm section **"Model Priority"** vào màn hình `SettingsScreen.kt` để user có thể thêm/xóa/sắp xếp/test model.

## Files to Modify

#### [MODIFY] `SettingsScreen.kt`

**Thay đổi:** Thêm section mới BÊN DƯỚI phần "Managed Keys" hiện tại.

### UI Layout chi tiết:

```
┌─────────────────────────────────────────────┐
│  ⬅ API Configuration                       │  ← Header (giữ nguyên)
├─────────────────────────────────────────────┤
│  [Phần API Keys hiện tại - GIỮA NGUYÊN]    │
├─────────────────────────────────────────────┤
│                                             │
│  Model Priority (4)              [↻ Reset]  │  ← Label + nút Reset Default
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │ [TextField: Nhập tên model...]  [+] │    │  ← Text Input + nút Add
│  └─────────────────────────────────────┘    │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │ #1 models/gemini-3.1-flash-lite... │    │
│  │ [Test ✓] [▼] [🗑]                  │    │  ← Item 1 (đầu tiên = không có ▲)
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ #2 models/gemini-3-flash-preview   │    │
│  │ [Test] [▲] [▼] [🗑]               │    │  ← Item 2
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ #3 models/gemini-2.5-flash-lite    │    │
│  │ [Test] [▲] [▼] [🗑]               │    │  ← Item 3
│  └─────────────────────────────────────┘    │
│  ┌─────────────────────────────────────┐    │
│  │ #4 models/gemini-2.5-flash         │    │
│  │ [Test] [▲] [🗑]                    │    │  ← Item cuối (không có ▼)
│  └─────────────────────────────────────┘    │
│                                             │
└─────────────────────────────────────────────┘
```

### Nút "Test" hoạt động như thế nào:

1. Lấy **API Key đầu tiên** từ `ApiKeyManager`.
2. Gửi request nhỏ tới `$BASE_URL/$model:generateContent` với prompt "Hi" và `maxOutputTokens: 1`.
3. Nếu response `200` → Hiện icon ✅ bên cạnh nút Test.
4. Nếu response lỗi → Hiện icon ❌ + Toast lỗi ngắn gọn.
5. Nếu chưa có API Key nào → Hiện Toast "Thêm API Key trước".

### Nút "Reset Default":
- Gọi `modelManager.resetToDefaults()`
- Refresh lại list

### Các State cần quản lý (remember):
```kotlin
var models by remember { mutableStateOf(modelManager.getModels()) }
var newModelName by remember { mutableStateOf("") }
var testResults by remember { mutableStateOf(mapOf<Int, Boolean?>()) } // index → true/false/null
var testingIndex by remember { mutableIntStateOf(-1) } // index đang test (-1 = không có)
```

### Styling:
- Reuse `GlassCard` component đã có
- Reuse color palette từ `ui/theme/` (DarkBackground, TextPrimary, TextSecondary, YouTubeRed)
- Nút ▲/▼ dùng `IconButton` với `Icons.Default.KeyboardArrowUp/Down`
- Nút Test dùng `OutlinedButton` nhỏ

## Implementation Steps
1. [ ] Thêm `ModelManager` vào SettingsScreen (remember { ModelManager.getInstance(context) })
2. [ ] Tạo section header "Model Priority" + nút Reset Default
3. [ ] Tạo Text Input row (TextField + nút Add)
4. [ ] Tạo LazyColumn item cho mỗi model (tên + Test + ▲/▼ + 🗑)
5. [ ] Implement logic nút Test (coroutine scope, OkHttp call nhỏ)
6. [ ] Implement reorder logic (moveUp/moveDown → refresh state)
7. [ ] Implement delete logic + empty state message

## Test Criteria
- [ ] Section "Model Priority" hiển thị bên dưới phần API Keys
- [ ] Thêm model mới hiện trong list
- [ ] Nút ▲/▼ thay đổi thứ tự
- [ ] Nút 🗑 xóa model
- [ ] Nút Test hiện ✅/❌ sau khi gọi API
- [ ] Nút Reset khôi phục 4 model mặc định
- [ ] List rỗng → hiện lại 4 model mặc định

---
Next Phase: → phase-04-verification.md
