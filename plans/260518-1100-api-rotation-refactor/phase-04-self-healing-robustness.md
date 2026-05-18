# Phase 04: Self-Healing & Robustness Verification
Status: ✅ Completed
Dependencies: Phase 03

## Objective
Tăng cường và kiểm chứng tính năng Tự động khôi phục (Self-Healing) của danh sách mô hình nhằm đảm bảo hệ thống luôn có ít nhất một mô hình khả dụng để thực hiện xoay tua, loại bỏ hoàn toàn khả năng ứng dụng bị rỗng tài nguyên hoặc lỗi crash lúc runtime theo yêu cầu của [rotation.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/rotation.md).

## Requirements
### Functional
- [x] Bảo đảm logic `saveModels` trong `ModelManager.kt` tự động ghi nhận danh sách mặc định `DEFAULT_MODELS` nếu nhận vào một danh sách rỗng (Self-Healing at storage level).
- [x] Khi xóa model cuối cùng từ UI (`SettingsScreen.kt`), danh sách ngay lập tức tự động hồi phục về `DEFAULT_MODELS` thay vì bị trống.
- [x] Đảm bảo tính an toàn dữ liệu 100%, không cho phép bất kỳ luồng hoạt động nào lưu cấu hình rỗng xuống SharedPreferences.

### Non-Functional
- [x] Đảm bảo tính nhất quán (Consistency) tuyệt đối giữa RAM, SharedPreferences và UI State.

## Implementation Steps
1. [x] Cập nhật hàm `saveModels` trong [ModelManager.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/main/java/com/skul9x/ytsummary/manager/ModelManager.kt) để tự động hóa việc phục hồi danh sách rỗng về mặc định.
2. [x] Viết các unit test xác minh khả năng Self-Healing của `ModelManager`.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/manager/ModelManager.kt` - Cập nhật logic lưu trữ bền vững tự hồi phục.

## Test Criteria (Bài test chi tiết)
Tạo file Unit Test tại `app/src/test/java/com/skul9x/ytsummary/manager/ModelManagerSelfHealingTest.kt` kiểm tra:
1. Khi gọi `saveModels(emptyList())`, SharedPreferences sẽ lưu JSON của `DEFAULT_MODELS` thay vì lưu mảng rỗng `[]`.
2. Khi liên tục xóa tất cả các phần tử khỏi danh sách, danh sách trả về từ `getModels()` tự động khôi phục lại đầy đủ 4 model mặc định chuẩn.

```kotlin
package com.skul9x.ytsummary.manager

import android.content.Context
import android.content.SharedPreferences
import io.mockk.*
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ModelManagerSelfHealingTest {

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
    fun testSaveModelsSelfHealsEmptyList() {
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } returns editor
        
        val manager = ModelManager.getInstance(context)
        
        // Act: Save an empty list
        manager.saveModels(emptyList())
        
        // Assert: It should save the DEFAULT_MODELS instead of []
        val savedList: List<String> = Json.decodeFromString(capturedJson.captured)
        assertEquals(ModelManager.DEFAULT_MODELS, savedList)
    }

    @Test
    fun testRemoveAllModelsTriggersSelfHealing() {
        // Mocking an initial list with 1 item
        var mockSavedData = "[\"models/gemini-2.5-flash\"]"
        every { sharedPrefs.getString(any(), null) } answers { mockSavedData }
        
        val capturedJson = slot<String>()
        every { editor.putString(any(), capture(capturedJson)) } answers {
            mockSavedData = capturedJson.captured
            editor
        }

        val manager = ModelManager.getInstance(context)
        
        // Act: Remove the last item (index 0)
        val result = manager.removeModel(0)
        
        // Assert: Success should be true
        assertEquals(true, result)
        
        // The saved JSON must now be the DEFAULT_MODELS JSON because of self-healing
        val savedList: List<String> = Json.decodeFromString(mockSavedData)
        assertEquals(ModelManager.DEFAULT_MODELS, savedList)
        
        // getModels() must return DEFAULT_MODELS
        assertEquals(ModelManager.DEFAULT_MODELS, manager.getModels())
    }
}
```

---
Next Steps: Kiểm tra và đánh giá lại toàn bộ kế hoạch để bắt đầu triển khai code thực tế.
