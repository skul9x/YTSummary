Status: ✅ Complete
Dependencies: Phase 01 (Cần URL Backend)

## Objective
Thiết lập nền tảng dự án Android, cấu hình Retrofit để giao tiếp với Backend và định nghĩa các Models cơ bản.

## Requirements
### Functional
- [ ] Khởi tạo dự án Android (Kotlin + Jetpack Compose).
- [ ] Cấu hình Retrofit & Gson để gọi Backend API.
- [ ] Định nghĩa `TranscriptResponse` và `Subtitle` models.

### Non-Functional
- [ ] Sử dụng Coroutines cho xử lý bất đồng bộ.
- [ ] Quản lý API Base URL trong `BuildConfig` hoặc `Constants`.

## Implementation Steps
1. [x] Setup Android Project mới (Package: `com.skul9x.ytsummary`).
2. [x] Thêm dependencies: Retrofit, OkHttp, Compose UI.
3. [x] Viết `YouTubeApiClient` interface.
4. [x] Tạo `NetworkModule` (hoặc Singleton đơn giản) để quản lý Retrofit instance.
5. [x] Viết unit test giả lập (MockWebServer) để kiểm tra việc parse dữ liệu từ Backend.

## Files to Create/Modify
- `app/build.gradle.kts` - Dependencies.
- `app/src/.../api/YouTubeApiClient.kt` - Retrofit Interface.
- `app/src/.../model/TranscriptResponse.kt` - Data models.
- `app/src/.../di/NetworkModule.kt` - Cấu hình Network.

## Test Criteria
- [ ] Unit test cho API call thành công (Mock response).
- [ ] App khởi động được màn hình trắng mà không crash.

---
Next Phase: [Phase 03: Key Rotation & AI Logic](phase-03-rotation-logic.md)
