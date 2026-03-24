# YTSummary

YTSummary là dự án tóm tắt video YouTube bằng AI, gồm ứng dụng Android (Kotlin + Jetpack Compose) và backend FastAPI để lấy transcript. Luồng chính:

1. Nhập link YouTube trên app.
2. Backend lấy transcript từ YouTube.
3. App gửi transcript sang Gemini để sinh tóm tắt tiếng Việt.
4. Hiển thị kết quả và lưu lịch sử.

## Tính năng chính

- Tóm tắt nội dung video YouTube bằng Gemini.
- Cơ chế xoay vòng API key/model để tăng độ ổn định khi quota giới hạn.
- Hỗ trợ lưu lịch sử tóm tắt.
- Backend proxy trung gian để tách luồng transcript khỏi mobile app.

## Cấu trúc thư mục

- `app/`: Ứng dụng Android (UI, repository, API client, lưu trữ local).
- `backend/`: FastAPI service (`main.py`, `requirements.txt`, `Dockerfile`).
- `docs/`: Tài liệu mô tả và báo cáo audit.
- `plans/`: Kế hoạch triển khai theo từng phase.
- `youtube-transcript-api-master/`: Bản local của thư viện transcript để tham khảo/tuỳ chỉnh.

## Yêu cầu môi trường

### Android

- Android Studio (bản mới).
- JDK 17.
- Gradle theo cấu hình trong project.

### Backend

- Python 3.10+ (khuyến nghị 3.11/3.12).
- `pip`.

## Cài đặt và chạy local

### 1) Chạy backend

```bash
cd backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

Mặc định backend chạy tại `http://0.0.0.0:8000` (hoặc port từ biến môi trường `PORT`).

Test nhanh:

```bash
curl "http://127.0.0.1:8000/api/transcript?video_id=dQw4w9WgXcQ"
```

### 2) Chạy app Android

1. Mở project trong Android Studio.
2. Build và chạy app.
3. Vào màn hình cài đặt để thêm Gemini API key.
4. Dán link YouTube và bắt đầu tóm tắt.

## Deploy backend (Railway)

- Source repo: `main` branch.
- Root directory: `/backend`.
- Dockerfile: `backend/Dockerfile`.
- Start command dùng trong container: `python main.py`.
- Domain production ví dụ: `https://ytsummary-production.up.railway.app`.

## Ghi chú vận hành

- Nếu gặp lỗi 502 trên Railway nhưng local chạy ổn, cần kiểm tra:
	- Runtime logs khi container start.
	- Cấu hình Root Directory có đúng `/backend` không.
	- Biến môi trường `PORT` và trạng thái healthcheck endpoint.

## Bản quyền

Copyright 2026 Nguyễn Duy Trường
