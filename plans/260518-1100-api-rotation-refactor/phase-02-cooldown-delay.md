# Phase 02: Cooldown Delay Implementation
Status: ✅ Completed
Dependencies: Phase 01

## Objective
Bổ sung cơ chế nghỉ ngắn vài trăm ms (delay) khi gặp lỗi HTTP 503 (Server Overload) hoặc lỗi HTTP 429 RPM (Rate Limit Per Minute) trước khi xoay vòng sang key tiếp theo trong lớp Orchestrator (`GeminiApiClient.kt`).

## Requirements
### Functional
- [x] Khi API trả về lỗi 503 hoặc 429 RPM, tiến hành mark cooldown cho cặp `(Model, Key)`.
- [x] Thực hiện một lệnh dừng bất đồng bộ ngắn (`delay(300L)`) để tránh gửi yêu cầu ồ ạt lên máy chủ trung gian khi xoay tua tài nguyên, tối ưu hóa nhịp điệu yêu cầu theo thiết kế [rotation.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/rotation.md).

### Non-Functional
- [x] Sử dụng `kotlinx.coroutines.delay` thay vì `Thread.sleep` để tránh block luồng chính của ứng dụng và tối ưu hiệu năng Coroutines.

## Implementation Steps
1. [x] Sửa đổi [GeminiApiClient.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt) tại phần bắt lỗi `ServerBusyException` hoặc trong catch block tương ứng.
2. [x] Thêm lệnh `delay(300L)` (cần import `kotlinx.coroutines.delay`).

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt` - Bổ sung `delay(300L)` khi nhận diện `ServerBusyException`.

## Test Criteria (Bài test chi tiết)
Tạo file Unit Test tại `app/src/test/java/com/skul9x/ytsummary/api/GeminiApiClientCooldownDelayTest.kt` kiểm tra:
1. Khi gọi API với key1 bị lỗi 503, hệ thống sẽ mark cooldown key1.
2. Hệ thống nghỉ ngắn (ít nhất 300ms virtual-time hoặc đo thực tế) trước khi thử gọi tiếp bằng key2.
3. Test case sử dụng `StandardTestDispatcher` từ `kotlinx-coroutines-test` để kiểm tra chính xác thời gian delay ảo (virtual time).

```kotlin
package com.skul9x.ytsummary.api

import com.skul9x.ytsummary.manager.ApiKeyManager
import com.skul9x.ytsummary.manager.ModelManager
import com.skul9x.ytsummary.manager.ModelQuotaManager
import com.skul9x.ytsummary.model.AiResult
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GeminiApiClientCooldownDelayTest {

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
    fun testServerBusyDelayIsApplied() = runTest {
        // Arrange: 2 keys, key1 returns 503, key2 returns 200
        every { apiKeyManager.getApiKeys() } returns listOf("key1", "key2")
        every { modelManager.getModels() } returns listOf("models/gemini-3.1-flash-lite")
        coEvery { quotaManager.isAvailable(any(), any()) } returns true
        coEvery { quotaManager.markCooldown(any(), "key1") } returns Unit

        val call1 = mockk<Call>()
        val response503 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(503)
            .message("Service Unavailable")
            .body("Server busy".toResponseBody(null))
            .build()
        
        val call2 = mockk<Call>()
        val response200 = Response.Builder()
            .request(Request.Builder().url("http://localhost").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("data: {\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Done!\"}]}}]}".toResponseBody(null))
            .build()
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key1" }) } returns call1
        every { call1.execute() } returns response503
        
        every { client.newCall(match { it.header("x-goog-api-key") == "key2" }) } returns call2
        every { call2.execute() } returns response200

        val startTime = currentTime
        
        // Act
        val results = apiClient.summarize("test").toList()

        val endTime = currentTime
        val elapsedTime = endTime - startTime

        // Assert
        assertTrue(results.any { it is AiResult.Success && it.text == "Done!" })
        // Check that delay of 300ms was applied between key1 and key2
        assertTrue("Expected delay of at least 300ms, but was $elapsedTime ms", elapsedTime >= 300L)
    }
}
```

---
Next Phase: [phase-03-network-termination.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-03-network-termination.md)
