# Phase 04: Frontend UI & Experience
Status: ✅ Complete
Dependencies: Phase 03

## Objective
Xây dựng giao diện người dùng đẹp mắt, premium bằng Jetpack Compose. Tập trung vào trải nghiệm mượt mà.

## Requirements
### Functional
- [x] Màn hình chính: Ô nhập link YouTube + Nút "Tóm tắt".
- [x] Màn hình Settings: Nơi dán danh sách API Keys (dạng text nhiều dòng).
- [x] Màn hình View Summary: Hiển thị Markdown kết quả tóm tắt.
- [x] Loading state: Hiệu ứng Shimmer hoặc Progress vui nhộn.
- [x] **Audio:** App Startup - Tự động chỉnh âm lượng Music lên 80%.
- [x] **TTS:** Nút Loa trên `SummaryScreen` để đọc to tóm tắt (đã lọc Markdown).

### UI/UX Rules (Antigravity Standard)
- [ ] Màu sắc: Dark mode chủ đạo (Vibe coding style).
- [ ] Animation: Chuyển cảnh mềm mại + Hiệu ứng bấm nảy (Bounce) cho nút Loa.
- [ ] Typography: Dùng font hiện đại (Inter/Outfit).

## Implementation Steps
1. [x] Thiết kế `MainScreen` với ô nhập URL.
2. [x] Thiết kế `SettingsScreen` quản lý Keys.
3. [x] Thiết kế `SummaryScreen` hỗ trợ Render Markdown.
4. [x] **Task 4.1:** App Startup - Set Music Volume to 80% (`AudioManager`) trong `MainActivity`.
5. [x] **Task 4.2:** TTS Helper - Viết util lọc Markdown và quản lý `TextToSpeech` engine.
6. [x] **Task 4.3:** UI - Thêm nút Loa vào `SummaryScreen` với hiệu ứng bấm nảy.
7. [x] Cài đặt `Navigation` giữa các màn hình.
8. [x] Thêm hiệu ứng feedback khi đang cào dữ liệu / đang tóm tắt.

## Files to Create/Modify
- `app/src/.../ui/MainScreen.kt`
- `app/src/.../ui/SettingsScreen.kt`
- `app/src/.../ui/SummaryScreen.kt`
- `app/src/.../theme/Color.kt` (Tinh chỉnh màu cực đẹp).

## Test Criteria
- [x] Người dùng dán được link YouTube vào và bấm nút.
- [x] Giao diện co giãn tốt trên nhiều kích thước màn hình.

---
Next Phase: [Phase 05: Deployment & Integration](phase-05-integration.md)
