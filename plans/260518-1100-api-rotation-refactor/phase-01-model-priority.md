Created At: 2026-05-18T04:02:26Z
Completed At: 2026-05-18T11:03:00Z
File Path: `file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-01-model-priority.md`

# Phase 01: Model Priority & Settings Align
Status: ✅ Completed
Dependencies: None

## Objective
Đồng bộ danh sách mô hình mặc định `DEFAULT_MODELS` trong `ModelManager.kt` để khớp chính xác với thứ tự ưu tiên (Model-First) được quy định trong đặc tả [rotation.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/rotation.md).

## Requirements
### Functional
- [x] Cập nhật `DEFAULT_MODELS` trong `ModelManager.kt` theo đúng thứ tự:
  1. `models/gemini-3.1-flash-lite`
  2. `models/gemini-2.5-flash-lite`
  3. `models/gemini-3-flash-preview`
  4. `models/gemini-2.5-flash`
- [x] Bảo đảm logic của `getModels()` hoạt động bình thường, trả về `DEFAULT_MODELS` mới khi chưa cấu hình hoặc cấu hình trống.

### Non-Functional
- [x] Bảo đảm tính tương thích ngược với dữ liệu cũ đã lưu (nếu có).

## Implementation Steps
1. [x] Sửa đổi `ModelManager.kt` tại [ModelManager.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/main/java/com/skul9x/ytsummary/manager/ModelManager.kt#L17-L22) thay thế `DEFAULT_MODELS` hiện tại bằng danh sách chuẩn hóa.
2. [x] Chạy và cập nhật các Unit Test trong [ModelManagerTest.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/test/java/com/skul9x/ytsummary/manager/ModelManagerTest.kt) để phản ánh sự thay đổi này.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/manager/ModelManager.kt` - Thay đổi giá trị `DEFAULT_MODELS`.
- `app/src/test/java/com/skul9x/ytsummary/manager/ModelManagerTest.kt` - Cập nhật các Assertions so khớp với `DEFAULT_MODELS`.

## Test Criteria (Bài test chi tiết)
Tạo file Unit Test tại `app/src/test/java/com/skul9x/ytsummary/manager/ModelManagerPriorityTest.kt` kiểm tra:
1. `DEFAULT_MODELS` chứa đúng 4 model theo thứ tự ưu tiên giảm dần.
2. Khi `SharedPreferences` trống, `getModels()` trả về chính xác danh sách này.

```kotlin
package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModelManagerPriorityTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    @Before
    fun setup() {
        every { context.applicationContext } returns context
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        
        // Reset singleton
        val field = ModelManager::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDefaultModelsPriorityMatchSpecification() {
        val expectedDefaults = listOf(
            "models/gemini-3.1-flash-lite",
            "models/gemini-2.5-flash-lite",
            "models/gemini-3-flash-preview",
            "models/gemini-2.5-flash"
        )
        
        assertEquals(expectedDefaults, ModelManager.DEFAULT_MODELS)
    }

    @Test
    fun testGetModelsReturnsSpecDefaultsWhenPrefsEmpty() {
        every { sharedPrefs.getString(any(), null) } returns null
        
        val manager = ModelManager.getInstance(context)
        val actualModels = manager.getModels()
        
        assertEquals(4, actualModels.size)
        assertEquals("models/gemini-3.1-flash-lite", actualModels[0])
        assertEquals("models/gemini-2.5-flash-lite", actualModels[1])
        assertEquals("models/gemini-3-flash-preview", actualModels[2])
        assertEquals("models/gemini-2.5-flash", actualModels[3])
    }
}
```

---
Next Phase: [phase-02-cooldown-delay.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-02-cooldown-delay.md)
