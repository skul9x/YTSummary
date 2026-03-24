# Plan: YouTube AI Summarizer (YTSummary)
Created: 2026-03-24
Status: 🟡 In Progress

## Overview
Xây dựng một ứng dụng Android cho phép tóm tắt nội dung video YouTube bằng AI (Gemini 2.0 Flash). Điểm đặc biệt của dự án là cơ chế xoay tua nhiều API Key để vượt qua giới hạn quota của Google AI Studio, kết hợp với một Backend Python trung gian để lấy transcript ổn định.

## Tech Stack
- **Frontend (Android):** Kotlin, Jetpack Compose, Retrofit, EncryptedSharedPreferences.
- **Backend (Python):** FastAPI, `youtube-transcript-api`, Uvicorn.
- **AI:** Google Gemini 2.0 Flash (Cloud SDK / REST).
- **Hosting:** Railway.app ($1 budget).

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | [Backend Development](phase-01-backend.md) | ✅ Complete | 100% |
| 02 | [Android Core Infrastructure](phase-02-android-core.md) | ✅ Complete | 100% |
| 03 | [Key Rotation & AI Logic](phase-03-rotation-logic.md) | ✅ Complete | 100% |
| 04 | [Frontend UI & Experience](phase-04-frontend-ui.md) | ✅ Complete | 100% |
| 05 | [Deployment & Integration](phase-05-integration.md) | 🟡 In Progress | 20% |
| 06 | [History & Persistence](phase-06-history-persistence.md) | ✅ Complete | 100% |

## Recent Audit 🩺
- **Status:** Done (2026-03-24)
- **Findings:** 2 Critical, 2 Warning. Fixing required before deployment.

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`
