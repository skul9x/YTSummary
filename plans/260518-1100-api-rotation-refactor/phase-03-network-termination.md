# Phase 03: Network Failure Termination
Status: ✅ Completed
Dependencies: Phase 02

## Objective
Nhận diện chính xác và dừng lập tức (terminate) tiến trình xoay tua API khi gặp lỗi kết nối vật lý (UnknownHostException, ConnectException) theo đặc tả kiến trúc [rotation.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/rotation.md).

## Requirements
### Functional
- [x] Khi thực hiện gọi API mà ném ra ngoại lệ `UnknownHostException` (không có DNS/Mạng) hoặc `ConnectException` (Không kết nối được server), tiến trình xoay tua phải dừng ngay lập tức.
- [x] Trả về một `AiResult.Error` chứa thông báo lỗi mạng thân thiện với người dùng (ví dụ: "Không có kết nối mạng. Vui lòng kiểm tra lại đường truyền.").
- [x] Tuyệt đối không tiếp tục lặp qua các key hoặc các model tiếp theo.

### Non-Functional
- [x] Đảm bảo tính nhanh nhạy và phản hồi trực quan tốt nhất khi người dùng mất mạng vật lý (UX Fail-Fast).

## Implementation Steps
1. [x] Sửa đổi [GeminiApiClient.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt) trong khối `catch` của hàm `summarize`.
2. [x] Bổ sung các `catch` block cụ thể cho `java.net.UnknownHostException` và `java.net.ConnectException` ngay trước khối catch `Exception` chung.
3. [x] Trong các catch block này, thực hiện `emit(AiResult.Error(...))` và `return@flow` để kết thúc quá trình stream.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt` - Bổ sung xử lý kết thúc luồng khi mất mạng vật lý.

## Test Criteria (Bài test chi tiết)
Tạo file Unit Test tại `app/src/test/java/com/skul9x/ytsummary/api/GeminiApiClientNetworkTerminationTest.kt` kiểm tra:
1. Khi gọi API với key1 và xảy ra lỗi `UnknownHostException`, luồng xoay tua lập tức dừng lại.
2. Không có bất kỳ lệnh gọi API nào được thực thi tiếp bằng key2.
3. Kết quả phát ra chứa lỗi mạng thân thiện với người dùng.

```kotlin
package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException

class GeminiApiClientNetworkTerminationTest {

    private val apiKeyManager = mockk<ApiKeyManager>()
    private val quotaManager = mockk<ModelQuotaManager>()
    private val modelManager = mockk<ModelManager>()
    private val client = mockk<OkHttpClient>()
    
    private lateinit var apiClient: GeminiApiClient

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.w(any(), any()) } returns 0

        apiClient = GeminiApiClient(
            apiKeyManager = apiKeyManager,
            quotaManager = quotaManager,
            modelManager = modelManager,
            client = client,
            baseUrl = "http://localhost"
        )
    }

    @Test
    fun testNetworkExceptionTerminatesRotationImmediately() = runTest {
        // Arrange: 2 keys, key1 throws UnknownHostException, key2 should NOT be called
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("models/gemini-3.1-flash-lite")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true

        val call1 = mockk<Call>()
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } throws UnknownHostException("No address associated with hostname")

        val call2 = mockk<Call>()
        // We will verify client.newCall is never invoked for key2
        
        // Act
        val results = apiClient.summarize("test").toList()

        // Assert
        assertEquals(1, results.size)
        assertTrue(results[0] is AiResult.Error)
        val errorMessage = (results[0] as AiResult.Error).message
        assertTrue(errorMessage.contains("mạng", ignoreCase = true))

        // Verify key2 was never called
        verify(exactly = 0) { client.newCall(match { it.header("x-goog-api-key") == "key2" }) }
    }
}
```

---
Next Phase: [phase-04-self-healing-robustness.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-04-self-healing-robustness.md)
