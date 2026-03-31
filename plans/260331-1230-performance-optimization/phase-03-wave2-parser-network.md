# Phase 03: Wave 2 - Parser & Network Tuning
Status: ✅ Completed (2026-03-31)
Dependencies: [Phase 02](phase-02-wave1-lazy-rendering.md)

## Objective
Tối ưu hóa tài nguyên hệ thống (CPU/RAM) trong quá trình xử lý transcript và quản lý kết nối mạng.

## Requirements
### Functional
- [x] Refactor `TranscriptParser` để giảm allocation và sử dụng regex tối ưu hơn.
- [x] Cấu hình Timeout linh hoạt cho OkHttpClient.

### Non-Functional
- [x] CPU: Giảm các đỉnh (spike) CPU khi parse bản phụ đề lớn.
- [x] Network: Tránh chiếm giữ socket quá lâu (long-held sockets).

## Implementation Steps
1. [x] **TranscriptParser**: Thay thế `Html.fromHtml` bằng giải pháp unescape nhanh hơn (ví dụ dùng String.replace cho các common entities) hoặc tối ưu hóa regex.
2. [x] **NetworkModule**: Tách biệt cấu hình Timeout: 15s cho Metadata/Connect, 60s+ cho Gemini Stream.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/transcript/TranscriptParser.kt`
- `app/src/main/java/com/skul9x/ytsummary/di/NetworkModule.kt`

## Test Criteria
- [x] Thời gian parse transcript giảm 20%+.
- [x] Memory Profiler không thấy các cột rác (GC) dày đặc khi parse.

---
Next Phase: [Phase 04: Release Build & Hardening](phase-04-wave3-build-config.md)
