# Phase 02: Room projection query for history list
Status: ✅ Completed
Dependencies: None

## Objective
Ngừng việc tải toàn bộ `summaryText` (nội dung tóm tắt) khi đang xem danh sách Lịch sử (History). Chỉ tải các field hiển thị để tối ưu I/O và RAM.

## Requirements
### Functional
- [x] Tạo `HistoryItem` DTO (videoId, title, thumbnailUrl, timestamp).
- [x] Cập nhật `SummaryDao.getAllSummaries` để tuyển chọn field (SELECT videoId, title...).
- [x] Cập nhật `HistoryScreen` sử dụng DTO mới.

### Non-Functional
- [x] Scrolling mượt mà dù history có > 100 mục.
- [x] Giảm Cursor Window size pressure.

## Implementation Steps
1. [x] Sửa `SummaryDao.kt` thêm phương thức trả về `HistoryItem`.
2. [x] Sửa `SummaryViewModel.kt` để đổi kiểu `PagingData`.
3. [x] Sửa `HistoryScreen.kt` UI model.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/data/SummaryDao.kt`
- `app/src/main/java/com/skul9x/ytsummary/model/HistoryItem.kt`
- `app/src/main/java/com/skul9x/ytsummary/ui/HistoryScreen.kt`

## Test Criteria
- [ ] Test cuộn danh sách History > 50 videos.
- [ ] Đảm bảo khi bấm vào item vẫn load đủ `summaryText` (Detail view).
