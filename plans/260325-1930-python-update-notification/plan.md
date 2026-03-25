# Plan: Python Update System Notification
Created: 2026-03-25T19:30:00Z
Status: 🟡 In Progress

## Overview
Thêm tính năng thông báo hệ thống (System Notification) khi phát hiện có cập nhật mới của thư viện `youtube-transcript-api`. Khi có bản mới (ví dụ: "Có bản update API mới, version 1.3.0"), app sẽ đẩy Notification giống email, nhấn vào sẽ mở app lên. Cần xin quyền POST_NOTIFICATIONS cho Android 13+ (API 33).

## Tech Stack
- Frontend: Jetpack Compose (Kotlin)
- Core: NotificationManagerCompat, Intent, PendingIntent
- Permissions: POST_NOTIFICATIONS (Android 13+)

## Phases

| Phase | Name | Status | Progress |
|-------|------|--------|----------|
| 01 | Permissions & Channel Setup | ⬜ Pending | 0% |
| 02 | Notification Helper | ⬜ Pending | 0% |
| 03 | ViewModel Integration | ⬜ Pending | 0% |
| 04 | Testing | ⬜ Pending | 0% |

## Quick Commands
- Start Phase 1: `/code phase-01`
- Check progress: `/next`
- Save context: `/save-brain`

## Completion
Tất cả các phase đã được hoàn tất:
- NotificationHelper đã được thiết lập với bộ đếm chống spam.
- Quyền POST_NOTIFICATIONS đã được khai báo và xin tại runtime với Android 13+.
- SummaryViewModel tích hợp gọi showUpdateNotification khi có info.
