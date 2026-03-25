# 💡 BRIEF: Custom Model Management

**Ngày tạo:** 2026-03-25  
**Brainstorm cùng:** skul9x

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT

Danh sách Model Gemini hiện đang **hard-code** trong [GeminiApiClient.kt](file:///home/skul9x/Desktop/Test_code/YTSummary-main/app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt#L120-L125):

```kotlin
val MODELS = listOf(
    "models/gemini-3.1-flash-lite-preview",
    "models/gemini-3-flash-preview",
    "models/gemini-2.5-flash-lite",
    "models/gemini-2.5-flash"
)
```

Người dùng **không thể tự quyết định** model nào ưu tiên, model nào dùng làm fallback ("hết nạc vạc tới xương"). Mỗi lần Google ra model mới, phải build lại app.

## 2. GIẢI PHÁP ĐỀ XUẤT

Thêm mục **"Model Priority"** trong Settings, cho phép user:
- **Thêm/Xóa** model tùy ý qua ô Text Input
- **Sắp xếp thứ tự ưu tiên** (nút lên/xuống) — model đầu danh sách được gọi trước
- **Test model** nhanh (gọi 1 token) trước khi lưu
- **Khôi phục mặc định** nếu lỡ xóa sạch hoặc muốn reset

## 3. ĐỐI TƯỢNG SỬ DỤNG

- **Primary:** Người dùng muốn tinh chỉnh trải nghiệm AI — chọn model nhanh/rẻ hay model mạnh
- **Secondary:** Power user muốn thử model mới ngay khi Google release mà không cần update app

## 4. QUYẾT ĐỊNH TỪ BRAINSTORM

| # | Câu hỏi | Quyết định |
|---|---------|------------|
| 1 | Default state | 4 model mặc định từ code hiện tại. Có nút "Khôi phục mặc định". Nếu xóa hết → tự xuất hiện lại 4 model mặc định |
| 2 | UX nhập model | **Option A** — Ô Text Input tự do, user copy/paste tên model |
| 3 | Scope cấu hình | Toàn app — Key 1 hay Key 2 đều chạy theo cùng thứ tự Model |
| 4 | Validation | Có nút **Test** — gọi nháp 1 token để kiểm tra model "sống" hay không |

## 5. TÍNH NĂNG

### 🚀 MVP (Bắt buộc có):

- [ ] **ModelManager** — Manager class lưu/đọc danh sách model vào SharedPreferences
- [ ] **Settings UI** — Section "Model Priority" với:
  - Ô text input + nút Add để thêm model mới
  - Danh sách model hiện tại có nút ▲/▼ (di chuyển thứ tự) và 🗑 (xóa)
  - Nút "Khôi phục mặc định"
- [ ] **GeminiApiClient** — Đọc model từ `ModelManager` thay vì hard-code `MODELS`
- [ ] **Test Model** — Nút "Test" bên cạnh mỗi model, gọi thử 1 request nhỏ để xác nhận model hợp lệ
- [ ] **Auto-fallback** — Nếu danh sách rỗng, tự động dùng 4 model mặc định

## 6. ƯỚC TÍNH SƠ BỘ

- **Độ phức tạp:** Trung bình
- **Files cần thay đổi:** ~4–5 files (1 file mới + 3–4 file sửa)
- **Rủi ro:**
  - User nhập sai tên model → Nút Test giải quyết đa phần, app vẫn fallback bình thường
  - Danh sách rỗng → Auto-fallback về default đảm bảo app không chết

## 7. BƯỚC TIẾP THEO

→ Chạy `/plan` để lên thiết kế chi tiết (file nào sửa gì, thứ tự implement)
