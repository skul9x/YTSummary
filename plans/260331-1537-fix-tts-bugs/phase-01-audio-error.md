Status: ✅ Complete
Dependencies: None

## Objective
Thêm Audio Focus listener để phản ứng khi ứng dụng khác chiếm quyền âm thanh và hoàn thiện hàm `onError` trong TTS engine.

## Implementation Steps
1. [x] Thêm `OnAudioFocusChangeListener` vào `TtsManager`. 
2. [x] Sửa lại `requestAudioFocus` để sử dụng listener.
3. [x] Hoàn thiện `onError` để decrement `pendingUtterances` và reset trạng thái.
4. [x] Thêm log debug cho `onError` và focus change.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/manager/TtsManager.kt`

## Test Criteria
- [ ] Mở app nhạc khi đang chạy TTS, icon UI chuyển sang trạng thái pause.
- [ ] Giả lập lỗi TTS, đảm bảo counter về 0 và không làm treo app.
