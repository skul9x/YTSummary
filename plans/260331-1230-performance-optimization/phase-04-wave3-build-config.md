# Phase 04: Wave 3 - Release Build & Hardening
Status: ✅ Completed (2026-03-31)
Dependencies: [Phase 03](phase-03-wave2-parser-network.md)

## Objective
Tối ưu hóa kích thước APK, bảo mật code và chuẩn bị hệ thống đo lường hiệu năng chuẩn chỉnh.

## Requirements
### Functional
- [x] Bật R8 (Minify/Shrink) cho bản Release.
- [x] Thêm Baseline Profiles.
- [x] Thiết lập Macrobenchmark module.

### Non-Functional
- [x] APK Size: Giảm kích thước APK tối thiểu 10%.
- [x] Startup Time: Giảm Cold Start time thêm 15%.

## Implementation Steps
1. [x] **build.gradle.kts**: Bật `isMinifyEnabled = true` và cấu hình Proguard/R8 rules.
2. [x] **Profiling**: Tạo module Macrobenchmark để đo lường frame time và startup time.
3. [x] **Baseline Profiles**: Generate baseline profiles để cải thiện hiệu năng JIT/AOT.

## Files to Create/Modify
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `benchmark/` (Module mới)

---
Next Phase: [Phase 05: Verification](phase-05-verification.md)
