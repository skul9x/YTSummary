# 💡 BRIEF: YouTube AI Summarizer (YTSummary)

**Ngày tạo:** 2026-03-24
**Trạng thái:** Brainstorming (Updated: Multi-Key Rotation & Plain Text)

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT
Người dùng muốn tóm tắt video YouTube bằng AI mà không bị giới hạn bởi giới hạn quota (429) của một API key duy nhất.

## 2. GIẢI PHÁP ĐỀ XUẤT (Hybrid Architecture)
Hệ thống sẽ chia phần việc "khó" (lấy transcript) cho Backend và phần "thông minh" (AI) cho Frontend.

- **Backend (Python FastAPI - Railway):**
    - Nhiệm vụ duy nhất: Lấy và làm sạch phụ đề (Loại timestamp, gộp text).
    - Tại sao? Vì `youtube-transcript-api` là Python-only và cần chạy trên server để ổn định.
- **Frontend (Android Kotlin):**
    - **Quản lý API Key:** Có UI để người dùng dán 1 danh sách API Keys.
    - **Xoay tua (Rotation):** Tự động chọn Key khả dụng. Nếu Key 1 bị lỗi 429 (hết quota), tự động chuyển sang Key 2, Key 3... theo chiến lược "Model-First" (giống RSS-Reader).
    - **Trực tiếp:** App Android gọi trực tiếp Gemini API bằng các Key này.

## 3. CƠ CHẾ XOAY TUA (ROTATION)
Dựa trên mẫu từ dự án RSS-Reader-main:
1. **ApiKeyManager:** Lưu trữ danh sách Key an toàn bằng `EncryptedSharedPreferences`.
2. **ModelQuotaManager:** Theo dõi trạng thái của từng cặp (Model + Key). Nếu bị lỗi 429, "cấm" Key đó trong 30 giờ.
3. **GeminiApiClient:** Tự động thử lại (Retry) với Key tiếp theo trong danh sách nếu Key hiện tại lỗi.

## 4. QUY TRÌNH HOẠT ĐỘNG
1. **User input URL** trên Android.
2. **Android -> Backend:** `GET /api/transcript?video_id=...`
3. **Backend -> Android:** Trả về Plain Text transcript "sạch".
4. **Android -> Gemini (AI Studio):** Gửi transcript này kèm Prompt tóm tắt. Nếu Key lỗi -> Đổi Key -> Thử lại.
5. **Display:** Hiển thị kết quả.

## 5. TÍNH NĂNG MVP
- [ ] Backend FastAPI (Railway) chỉ lấy Transcript.
- [ ] UI Android: Setting dán multi-key, parse tự động.
- [ ] Logic xoay tua Key + Quản lý Quota (ModelQuotaManager).
- [ ] Hiển thị bản tóm tắt đẹp mắt.

---
**Giải pháp này giúp app của anh "bất tử" trước các giới hạn free của Google AI Studio! Anh thấy kế hoạch này đã sát với RSS-Reader chưa ạ?**
