# Phase 01: Refactor State Management
Status: ⬜ Pending
Dependencies: None

## Objective
Tách biệt việc quản lý trạng thái phát (TTS State) ra một cấu trúc dữ liệu chặt chẽ hơn trong `TtsManager.kt` để chuẩn bị cho việc tối ưu tracking.

## Implementation Steps
1. [ ] Cập nhật `TtsManager.kt`: Thêm một Data class nội bộ `TtsProgress` để chứa `totalSpoken`, `currentIndex`, và `activeChunkId`.
2. [ ] Sửa đổi `onStart` callback: Ghi nhận chính xác `utteranceId` của chunk đang chạy vào `TtsProgress`.
3. [ ] Thêm Logging chi tiết: Log mọi thay đổi biến số trong `onStart`, `onDone`, `onRangeStart` kèm timestamp độ phân giải cao (ms).

## Files to Create/Modify
- `com.skul9x.ytsummary.manager.TtsManager.kt` - Cấu trúc lại các biến state và thêm logging.

## Test Criteria
- [ ] Log hiển thị chính xác tiến trình chuyển đổi giữa các chunk.
- [ ] Không làm hỏng tính năng Play hiện tại.
