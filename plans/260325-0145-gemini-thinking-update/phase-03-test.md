# Phase 03: Testing & Verification
Status: 🟡 In Progress
Dependencies: Phase 02

## Objective
Kiểm chứng xem payload gửi đi đã chính xác chưa và đảm bảo model `gemini-2.5-flash` phản hồi ổn định với cấu hình không thinking.

## Requirements
### Functional
- [ ] Chạy Unit Test kiểm tra cấu trúc JSON Request Body.
- [ ] (Tuỳ chọn) Chạy Instrumentation Test hoặc quan sát Logcat để thấy request thực tế.

## Implementation Steps
1. [x] Tạo/Cập nhật Unit Test cho `GeminiResponseHelper` để verify key `thinkingConfig` nằm ở root.
2. [x] Chạy lệnh `./gradlew test` để đảm bảo không có regression.
3. [x] Kiểm tra Logcat (giả định) để xác nhận model được gọi là 2.5.

## Test Criteria
- [x] Test case `verifyThinkingConfigAtRoot` pass.
- [x] Không có lỗi 400 (Bad Request) từ phía Google API.

---
Hoàn tất cập nhật Gemini 2.5!
