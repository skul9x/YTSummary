# Plan: Fix TTS Robustness & Boundary Racing
Created: 2026-03-31T15:48:00
Status: 🟡 In Progress

## Overview
Dựa trên phân tích từ `bugs.txt`, project cần giải quyết triệt để các vấn đề ranh giới (boundary) và race condition khi Pause/Resume TTS. Thay đổi chính là chuyển sang quản lý "Active Chunk" chủ động thay vì parse ID bị động trong callback.

## Tech Stack
- Frontend: Jetpack Compose (SummaryScreen)
- Backend: Android Text-To-Speech (TTS)
- Database: Room (SummaryEntity - resume index storage)

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Refactor State Management | ✅ Complete | 100% |
| 02 | Robust Tracking Implementation | ✅ Complete | 100% |
| 03 | Boundary Safety & Guard | ✅ Complete | 100% |
| 04 | Validation & Stress Test | ✅ Complete | 100% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`
