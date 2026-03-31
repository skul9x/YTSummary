# Phase 01: Wave 1 - Critical Fixes
Status: ⬜ Pending
Dependencies: None

## Objective
Khắc phục 2 lỗi nghiêm trọng nhất ảnh hưởng đến độ ổn định của app: Thread Blocking trong Retry và Recomposition Scope quá rộng.

## Requirements
### Functional
- [ ] Loại bỏ `Thread.sleep` trong `RetryInterceptor`.
- [ ] Tách nhỏ State consumption trong `MainActivity`.

### Non-Functional
- [ ] Performance: Giảm blocking worker threads.
- [ ] UI: Giảm số lượng recompositions khi track TTS hoặc stream Gemini.

## Implementation Steps
1. [ ] **RetryInterceptor**: Chuyển sang mô hình retry ở tầng Repository/UseCase sử dụng Coroutine `delay` (không block thread).
2. [ ] **MainActivity**: Cập nhật `setContent` để chỉ collect những field cần thiết cho từng Composable thay vì collect nguyên cục `UiState`.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/network/RetryInterceptor.kt`
- `app/src/main/java/com/skul9x/ytsummary/ui/MainActivity.kt`
- `app/src/main/java/com/skul9x/ytsummary/ui/SummaryViewModel.kt`

## Test Criteria
- [ ] Network retry vẫn hoạt động đúng với Exponential Backoff.
- [ ] App không bị lag/treo khi gặp lỗi 5xx.
- [ ] Layout Inspector xác nhận số lần Recomposition giảm ở các màn hình con.

---
Next Phase: [Phase 02: Lazy Summary Rendering](phase-02-wave1-lazy-rendering.md)
