# 🎥 YTSummary - YouTube AI Summarizer

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/android)
[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Backend](https://img.shields.io/badge/Backend-FastAPI-teal.svg)](https://fastapi.tiangolo.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**YTSummary** là một giải pháp toàn diện cho việc tóm tắt nội dung video YouTube bằng trí tuệ nhân tạo (Gemini AI). Dự án bao gồm một ứng dụng Android hiện đại và một backend proxy mạnh mẽ để xử lý dữ liệu từ YouTube.

---

## ✨ Điểm nổi bật (Highlights)

- ⚡ **Tóm tắt siêu tốc**: Sử dụng Gemini 1.5 Flash cho hiệu suất nhanh nhất và Gemini 1.5 Pro cho độ chính xác cao nhất.
- 📺 **Hỗ trợ đa ngôn ngữ**: Tự động ưu tiên lấy phụ đề Tiếng Việt và Tiếng Anh từ mọi video ID.
- 🛡️ **Bảo mật tối đa**: Cơ sở dữ liệu Room được mã hóa bằng **SQLCipher** (AES-256) và proxy backend có giới hạn tần suất (Rate Limiting).
- 🔄 **Xoay vòng API**: Cơ chế `ApiKeyManager` và `ModelQuotaManager` thông minh giúp ứng dụng hoạt động ổn định kể cả khi vượt quá hạn ngạch.
- 🎨 **Material 3 Design**: Giao diện người dùng (UI) tối giản, hiện đại, hỗ trợ Dynamic Color (Android 12+).

---

## 🏗️ Cấu trúc dự án

Dự án được phân cấp rõ ràng theo mô hình Client-Server:

```text
├── app/                      # Mã nguồn ứng dụng Android (Kotlin & Jetpack Compose)
│   ├── src/main/java         # Logic xử lý, ViewModel, Repository
│   └── build.gradle.kts      # Cấu hình Gradle và Dependencies
├── backend/                  # API Proxy Backend (FastAPI)
│   ├── main.py               # API endpoints lấy Transcript & Metadata
│   ├── requirements.txt      # Thư viện Python cần thiết
│   └── Dockerfile            # Cấu hình triển khai (Deployment)
├── .brain/                   # Hệ thống tri thức và lịch sử quyết định (AI context)
└── docs/                     # Tài liệu thiết kế và đặc tả kỹ thuật
```

---

## 🚀 Hướng dẫn cài đặt

### 1. Backend (Python/FastAPI)
- Yêu cầu Python 3.9+
- Cài đặt thư viện:
  ```bash
  cd backend
  pip install -r requirements.txt
  ```
- Chạy máy chủ nội bộ:
  ```bash
  uvicorn main:app --reload
  ```

### 2. Android App (Kotlin/Jetpack Compose)
- Mở thư mục dự án bằng **Android Studio Ladybug** (hoặc mới hơn).
- Đồng bộ hóa Gradle (Sync Project with Gradle Files).
- Chạy ứng dụng trên Emulator hoặc thiết bị thật (API level 26+).

---

## 🛠️ Công nghệ sử dụng

- **Frontend**: Jetpack Compose, Retrofit, OkHttp, Coil, Room Database, SQLCipher, Coroutines, Navigation Compose.
- **Backend**: FastAPI, YouTube Transcript API, Httpx, SlowAPI (Rate Limiter), Uvicorn.
- **AI Engine**: Google Gemini API (Flash & Pro).

---

## 📅 Kế hoạch phát triển (Roadmap)

- [x] Hỗ trợ lấy phụ đề tự động.
- [x] Tóm tắt bằng AI (Gemini).
- [x] Quản lý API Key xoay vòng.
- [x] Thiết kế UI/UX Material 3.
- [ ] Tính năng tải video/audio tóm tắt.
- [ ] Chế độ Offline cho các nội dung đã tóm tắt.
- [ ] Đa dạng hóa các mô hình AI khác (Claude, ChatGPT).

---

## 📄 Bản quyền (Copyright)

Copyright 2026 Nguyễn Duy Trường

Mọi quyền được bảo lưu. Dự án được phát triển nhằm mục đích cá nhân và giáo dục.

---

> [!TIP]
> **Mẹo**: Hãy kiểm tra phần [CHANGELOG.md](CHANGELOG.md) để biết các cập nhật mới nhất của dự án!
