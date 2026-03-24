# 💡 YouTube AI Summarizer (YTSummary)

[![FastAPI](https://img.shields.io/badge/Backend-FastAPI-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Kotlin](https://img.shields.io/badge/Android-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![AI](https://img.shields.io/badge/AI-Gemini_1.5_Flash-green?logo=google-gemini&logoColor=white)](https://deepmind.google/technologies/gemini/)

**YTSummary** là một giải pháp tóm tắt video YouTube thông minh sử dụng trí tuệ nhân tạo (AI). Dự án kết hợp sức mạnh của **FastAPI** (Python) để xử lý dữ liệu video và **Android** (Kotlin) để mang lại trải nghiệm người dùng mượt mà, bảo mật và linh hoạt.

---

## 🚀 Tính Năng Nổi Bật

- **Tóm tắt thông minh**: Sử dụng model **Gemini 1.5 Flash** mới nhất để trích xuất ý chính từ video YouTube dài chỉ trong vài giây.
- **Xoay tua API Key (Rotation)**: Cơ chế "Model-First" cho phép thêm nhiều API Key cùng lúc, tự động đổi Key khi hết quota, giúp ứng dụng hoạt động không giới hạn.
- **Bảo mật tối đa**: Lưu trữ API Key bằng `EncryptedSharedPreferences` và mã hóa cơ sở dữ liệu Room bằng **SQLCipher**.
- **Giao diện hiện đại**: Thiết kế **Glassmorphism** (kính mờ) sang trọng, hỗ trợ hiệu ứng chuyển cảnh mượt mà.
- **Hỗ trợ đa ngôn ngữ**: Tự động lấy transcript tiếng Việt (ưu tiên) hoặc tiếng Anh.
- **Tích hợp Audio (TTS)**: Tính năng đọc bản tóm tắt thành tiếng với khả năng làm sạch văn bản thông minh.
- **Quản lý lịch sử**: Lưu lại các bản tóm tắt cũ để xem lại mọi lúc, ngay cả khi ngoại tuyến.

---

## 🏗️ Cấu Trúc Dự Án

Dự án sử dụng kiến trúc **Hybrid**:
- **`/backend`**: Server proxy viết bằng Python FastAPI, chịu trách nhiệm lấy và làm sạch Transcript từ YouTube (đã deploy trên Railway).
- **`/app`**: Ứng dụng Android viết bằng Kotlin Jetpack Compose, chịu trách nhiệm gọi Gemini AI và quản lý trải nghiệm người dùng.
- **`/.brain`**: Hệ thống lưu trữ kiến thức và tiến độ của AI hỗ trợ phát triển (AWF Framework).
- **`/docs`**: Các tài liệu thiết kế chi tiết, brief dự án và báo cáo audit bảo mật.

---

## 🛠️ Hướng Dẫn Cài Đặt

### 1. Backend (Nếu muốn chạy local)
```bash
cd backend
python -m venv venv
source venv/bin/activate  # Hoặc venv\Scripts\activate trên Windows
pip install -r requirements.txt
python main.py
```

### 2. Frontend (Android)
- Mở thư mục dự án bằng **Android Studio (Ladybug hoặc mới hơn)**.
- Đảm bảo bạn đã cài đặt Android SDK level 35.
- Android app mặc định sẽ gọi lên URL Production trên Railway. Nếu muốn thay đổi, hãy chỉnh sửa trong `app/src/main/java/com/skul9x/ytsummary/utils/Constants.kt`.
- Build và cài đặt APK lên thiết bị của bạn.

---

## 📝 Cách Sử Dụng

1.  **Nhập API Keys**: Vào mục Settings, dán danh sách các Gemini API Keys từ Google AI Studio (mỗi dòng một key).
2.  **Tóm tắt**: Copy link video YouTube bất kỳ, dán vào màn hình chính và nhấn "Tóm tắt".
3.  **Nghe tóm tắt**: Nhấn vào icon loa để kích hoạt tính năng đọc âm thanh (TTS).
4.  **Xem lịch sử**: Các video đã tóm tắt sẽ tự động lưu trong tab "Lịch sử".

---

## 🔒 Bảo Mật & Quy Tắc
- Không bao giờ push API Key thật lên repository này.
- Mọi dữ liệu nhạy cảm được mã hóa local bằng chuẩn quân đội (SQLCipher).
- Hệ thống hỗ trợ xoay tua model tự động để tối ưu hóa chi phí và hiệu suất.

---

## 📄 License & Copyright

Copyright 2026 Nguyễn Duy Trường

*Dự án này được phát triển bởi Nguyễn Duy Trường. Mọi hành vi sao chép hoặc phát hành lại cần có sự cho phép của tác giả.*
