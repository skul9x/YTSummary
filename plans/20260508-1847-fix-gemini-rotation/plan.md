# Plan: Fix Gemini API Rotation Logic
Created: 2026-05-08T18:47:00
Status: 🟡 In Progress

## Overview
This plan aims to address 3 critical logic flaws in the Gemini API rotation system:
1.  **Rate Limit vs Daily Quota:** Distinguish between 429 RPM (temporary cooldown) and 429 RPD (long-term ban).
2.  **Streaming Integrity:** Prevent duplicate text in the UI by disabling retries once the stream has started.
3.  **Model Fallback Optimization:** Immediately switch models when encountering client-side errors (400/404) instead of cycling through all keys for the same faulty model/request.

## Tech Stack
- Language: Kotlin
- Networking: OkHttp, Coroutines Flow
- Platform: Android

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Diagnostics & Verification | ✅ Done | 100% |
| 02 | API Client Refactoring | ⬜ Pending | 0% |
| 03 | Validation & Integration Testing | ⬜ Pending | 0% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`
