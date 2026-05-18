# Plan: API Rotation & Quota Management Alignment
Created: 2026-05-18
Status: 🟡 In Progress

## Overview
Kế hoạch này chi tiết hóa việc nâng cấp và tái cấu trúc (refactor) cơ chế xoay tua API (API Rotation) và quản lý hạn ngạch (Quota Management) của ứng dụng Kotlin để khớp hoàn toàn (100% compliant) với đặc tả kiến trúc trong [rotation.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/rotation.md).

## Core Alignments
1. **Danh sách & Thứ tự ưu tiên Model**: Đồng bộ danh sách mặc định `DEFAULT_MODELS` trong `ModelManager.kt` với đúng đặc tả (gemini-3.1-flash-lite đứng đầu, tiếp theo là gemini-2.5-flash-lite, gemini-3-flash-preview và gemini-2.5-flash).
2. **Nghỉ ngắn vài trăm ms khi lỗi 503 / 429 RPM**: Bổ sung cơ chế `delay(300L)` khi mark cooldown (Server Busy) trong `GeminiApiClient.kt` để giãn cách các cuộc gọi xoay tua liên tục, tránh làm nghẽn hoặc quá tải hệ thống.
3. **Mất mạng vật lý thì dừng ngay (Termination)**: Nhận diện chính xác các lỗi kết nối vật lý như `UnknownHostException` hay `ConnectException` để lập tức kết thúc luồng xoay tua thay vì tiếp tục lặp lại các key/model vô ích.
4. **Tự động khôi phục (Self-Healing)**: Tăng cường tính năng tự động hồi phục (Reset to Defaults) khi danh sách model bị trống hoặc lỗi phân tích cú pháp để đảm bảo hệ thống không bao giờ bị nghẽn (Zero-Crash Fallback).

---

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| [01](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-01-model-priority.md) | Model Priority & Settings Align | ⬜ Pending | 0% |
| [02](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-02-cooldown-delay.md) | Cooldown Delay Implementation | ⬜ Pending | 0% |
| [03](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-03-network-termination.md) | Network Failure Termination | ⬜ Pending | 0% |
| [04](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-04-self-healing-robustness.md) | Self-Healing & Robustness Verification | ⬜ Pending | 0% |

---

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`

---
Next Phase: [phase-01-model-priority.md](file:///home/skul9x/Desktop/Test_code/YTSummary-main/plans/260518-1100-api-rotation-refactor/phase-01-model-priority.md)
