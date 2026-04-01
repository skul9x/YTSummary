# Phase 03: Boundary Safety & Guard
Status: ⬜ Pending
Dependencies: Phase 02

## Objective
Xử lý trường hợp "xấu nhất": User nhấn Pause ngay lúc TTS vừa xong đoạn A và chuẩn bị nói đoạn B (Boundary Pause).

## Implementation Steps
1. [ ] Implement `isTtsActive` flag: Kiểm tra trạng thái thực tế của TTS engine (`tts.isSpeaking`) bổ trợ cho `pendingUtterances`.
2. [ ] Thêm Guard Logic cho `pause()`: Nếu `tts.stop()` được gọi khi `isSpeaking == false` nhưng `pendingUtterances > 0`, trả về vị trí `totalSpokenLength` của chunk kế tiếp để resume sạch sẽ.
3. [ ] Xử lý Resume `fromIndex`: Đảm bảo khi resume, text được substring chính xác và không lặp từ cuối cùng của chunk trước.

## Files to Create/Modify
- `TtsManager.kt` - Logic `pause()` bảo vệ ranh giới.

## Test Criteria
- [ ] Pause tại đúng ranh giới của hai chunk và resume lại không bị lặp hay mất chữ.
