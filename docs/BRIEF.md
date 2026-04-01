# 💡 BRIEF: Quick Volume Toggle Feature

**Ngày tạo:** 2026-04-01
**Brainstorm cùng:** skul9x

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT
Khi người dùng xem tóm tắt video và nghe AI đọc (TTS), việc điều chỉnh âm lượng bằng phím cứng (volume buttons) có thể bất tiện hoặc không đủ nhanh để đạt mức âm lượng mong muốn. Người dùng cần một cách nhanh chóng để đặt âm lượng về các mức tối ưu (80%, 85%, 90%) ngay trên giao diện.

## 2. GIẢI PHÁP ĐỀ XUẤT
Thêm một nút điều khiển âm lượng thông minh tại màn hình "AI Analysis" (Summary Screen). Nút này sẽ cho phép người dùng xoay vòng giữa 3 mức âm lượng cố định bằng cách chạm.

## 3. ĐỐI TƯỢNG SỬ DỤNG
- **Primary:** Người dùng thường xuyên sử dụng tính năng TTS (Text-to-Speech) để nghe tóm tắt video.

## 4. ĐIỂM KHÁC BIỆT
- Không đồng bộ (Sync) liên tục với âm lượng hệ thống để tránh gây phiền nhiễu.
- Chỉ tác động khi người dùng chủ động nhấn nút.
- Thiết kế tối giản, trực quan ngay trên thanh công cụ/góc màn hình.

## 5. TÍNH NĂNG ĐỀ XUẤT (Vibe Coding)

### 🚀 MVP (Bắt buộc có):
- [ ] **Giao diện nút:** Icon loa + con số (Ví dụ: 🔊 85).
- [ ] **Vị trí:** Góc trên cùng bên phải của màn hình Summary.
- [ ] **Logic xoay vòng:** 80% (Mặc định) → 85% → 90% → 80%.
- [ ] **Tác động:** Điều chỉnh `AudioManager.STREAM_MUSIC` (Media Volume) khi nhấn.
- [ ] **Reset:** Luôn quay về mức 80% khi khởi động lại màn hình/ứng dụng (Stateless persistence).

### 🎁 Phase 2 (Làm sau):
- [ ] Hiệu ứng âm thanh nhỏ (beep) khi đạt mức 90%.
- [ ] Animation rung nhẹ hoặc phóng to khi click.

## 6. ƯỚC TÍNH SƠ BỘ
- **Độ phức tạp:** 🟢 **Dễ (Vài giờ)** - Chỉ cần can thiệp vào UI (Compose) và `AudioManager`.
- **Rủi ro:** Cần xin quyền "Do Not Disturb" nếu muốn ghi đè âm lượng trong một số trường hợp Android đặc biệt, nhưng với Media Volume thông thường thì không cần.

## 7. BƯỚC TIẾP THEO
→ Chạy `/plan` để lên thiết kế chi tiết về code (Compose UI & ViewModel logic).
