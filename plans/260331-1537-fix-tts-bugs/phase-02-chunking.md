Status: ✅ Complete
Dependencies: Phase 01

## Objective
Thêm logic `speakInChunks` để tự động chia nhỏ văn bản dài (>3500 ký tự) và gửi từng đoạn vào TTS engine.

## Implementation Steps
1. [x] Thêm hàm private `speakInChunks(fullText, startIndex)`.
2. [x] Thêm logic cắt text tại dấu câu (dấu chấm, dấu phẩy, \n) cuối mỗi chunk.
3. [x] Cập nhật `speak` để sử dụng `speakInChunks`.
4. [x] Sửa đổi `pendingUtterances` để `incrementAndGet()` từng chunk mới.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt`

## Test Criteria
- [ ] Test với văn bản dài > 10,000 ký tự.
- [ ] Quan sát `onDone` được gọi bao nhiêu lần cho từng `utteranceId`.
