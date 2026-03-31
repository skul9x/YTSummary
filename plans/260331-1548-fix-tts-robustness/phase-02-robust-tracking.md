# Phase 02: Robust Tracking Implementation
Status: ⬜ Pending
Dependencies: Phase 01

## Objective
Thay đổi cơ chế cập nhật `totalSpokenLength` sao cho ít phụ thuộc nhất vào callback bên thứ ba nhằm tránh race condition khi pause.

## Implementation Steps
1. [ ] Cập nhật `speakInChunks`: Khi enqueue một chunk, đồng thời đẩy "Expected Offset" của chunk đó vào một `Map<String, Int>` (với key là `utteranceId`).
2. [ ] Sửa đổi `onDone`: Cập nhật `totalSpokenLength` bằng cách tra cứu từ `Map` trên thay vì parse chuỗi `utteranceId`.
3. [ ] Dọn dẹp `Map`: Xóa các record cũ ngay sau khi `onDone` hoặc `onError`.

## Files to Create/Modify
- `TtsManager.kt` - Logic map tracking chunk.

## Test Criteria
- [ ] `totalSpokenLength` luôn đúng kể cả khi `utteranceId` bị biến đổi hoặc không đồng bộ.
- [ ] Logic parse cũ vẫn được giữ lại làm fallback.
