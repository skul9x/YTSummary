# Phase 06: History & Persistence
Status: ⬜ Pending
Dependencies: Phase 04

## Objective
Lưu trữ lịch sử tóm tắt video để người dùng có thể xem lại nhanh chóng mà không cần tốn tài nguyên (AI/Network) nhiều lần. 

## Requirements
### Functional
- [ ] Lưu trữ bản tóm tắt thành công vào SQLite (Room DB).
- [ ] Hiển thị danh sách lịch sử tại màn hình chính hoặc một màn hình riêng.
- [ ] Cho phép xem lại bản tóm tắt cũ từ danh sách lịch sử.
- [ ] Cho phép xóa lịch sử (từng mục hoặc tất cả).

### Technical
- [ ] Framework: Android Room Persistence Library.
- [ ] Schema: `id`, `video_id`, `title`, `thumbnail_url`, `summary_text`, `timestamp`.
- [ ] Repository: Tự động lưu sau mỗi lần tóm tắt thành công.

## Implementation Steps
### 1. Database Setup
- [ ] Tạo `SummaryEntity.kt` (Table Schema).
- [ ] Tạo `SummaryDao.kt` (Truy vấn: Insert, Delete, GetAll).
- [ ] Tạo `AppDatabase.kt` (Room Database instance).

### 2. Repository Integration
- [ ] Cập nhật `SummarizationRepository.kt` để inject `SummaryDao`.
- [ ] Logic: Khi AI trả về `Success` -> Lưu vào DB.
- [ ] Logic: Thêm hàm lấy dữ liệu từ DB (getAllHistory).

### 3. UI Implementation
- [ ] Thiết kế `HistoryScreen.kt` (Glassmorphism List).
- [ ] Thêm nút "History" vào `MainScreen`.
- [ ] Thiết lập Điều hướng (Navigation) qua `HistoryScreen`.

## Files to Create/Modify
- `app/src/.../data/local/SummaryEntity.kt`
- `app/src/.../data/local/SummaryDao.kt`
- `app/src/.../data/local/AppDatabase.kt`
- `app/src/.../repository/SummarizationRepository.kt`
- `app/src/.../ui/HistoryScreen.kt`

## Test Criteria
- [ ] Tóm tắt 1 video -> Quay ra thấy xuất hiện trong lịch sử.
- [ ] Tắt app mở lại -> Lịch sử vẫn còn.
- [ ] Click vào 1 mục trong lịch sử -> Chuyển đến `SummaryScreen` với nội dung cũ.
