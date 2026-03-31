Status: ✅ Complete
Dependencies: Phase 02

## Objective
Thêm các dòng log debug chi tiết để theo dõi luồng dữ liệu trên Logcat và thực hiện kiểm tra cuối cùng với các mức độ dài văn bản khác nhau.

## Implementation Steps
1. [x] Thêm log `Utterance START` trong hàm `onStart` của `UtteranceProgressListener`.
2. [x] Thêm log `Utterance DONE` trong hàm `onDone` (đã có một phần, cần hoàn thiện).
3. [x] Kiểm tra logic `onTtsDone` để chắc chắn nó chỉ chạy khi `pendingUtterances` thực sự về 0.
4. [x] Thực hiện dọn dẹp các ghi chú (TODO) hoặc biến thừa nếu còn sót lại.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt`

## Test Criteria
- [ ] Logcat hiển thị đúng thứ tự: START chunk_0 -> DONE chunk_0 -> START chunk_3500 -> ... -> Callback `onTtsDone` được gọi.
- [ ] Test với văn bản cực ngắn (1 câu) và văn bản cực dài (>10,000 ký tự).
