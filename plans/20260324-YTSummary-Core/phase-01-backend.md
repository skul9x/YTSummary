# Phase 01: Backend Development (FastAPI)
Status: ⬜ Pending
Dependencies: None

## Objective
Xây dựng một service Python (FastAPI) chuyên trách việc trích xuất phụ đề từ YouTube Video ID. Service này sẽ được tối ưu để hoạt động ổn định trên Railway.app.

## Requirements
### Functional
- [ ] Nhận `video_id` qua query parameter `GET /api/transcript?video_id=...`.
- [ ] Trả về Plain Text transcript (đã làm sạch timestamp).
- [ ] Hỗ trợ lấy tiếng Việt (`vi`) hoặc tiếng Anh (`en`) làm fallback.
- [ ] Xử lý lỗi: Video không có sub, Video bị giới hạn độ tuổi, Error 429 từ YouTube.

### Non-Functional
- [ ] Cấu hình CORS cho phép Android App gọi.
- [ ] Tối ưu RAM/CPU để chạy trong gói $1/tháng trên Railway.

## Implementation Steps
1. [ ] Khởi tạo dự án Python với `main.py` và `requirements.txt`.
2. [ ] Cài đặt `fastapi`, `uvicorn`, `youtube-transcript-api`.
3. [ ] Viết logic bóc tách transcript trong `main.py`.
4. [ ] Viết script test local để đảm bảo trả về text sạch.
5. [ ] Tạo Dockerfile hoặc cấu hình Railway Start Command.
6. [ ] Deploy lên Railway.app và lấy URL production.

## Files to Create/Modify
- `backend/main.py` - Logic API chính.
- `backend/requirements.txt` - Danh sách thư viện.
- `backend/test_api.py` - Script test nhanh.

## Test Criteria
- [ ] `GET /api/transcript?video_id=dQw4w9WgXcQ` trả về text "Never Gonna Give You Up...".
- [ ] Gọi API với video ID không tồn tại trả về lỗi 404.
- [ ] API phản hồi < 2s.

---
Next Phase: [Phase 02: Android Core Infrastructure](phase-02-android-core.md)
