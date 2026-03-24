# Changelog - YouTube AI Summarizer (YTSummary)

## [2026-03-24] - Post-Deployment (Final URL) ✅
### Added
- **Constants.kt**: Created `app/src/main/java/com/skul9x/ytsummary/utils/Constants.kt` for centralized URL management.
- **Production URL Sync**: Updated Android `NetworkModule` to call Railway Production URL (`https://ytsummary-production.up.railway.app/`).

## [2026-03-24] - Production Deployment ✅
### Added
- **Railway Deployment**: Successfully deployed FastAPI backend to Railway.app.
- **Production URL**: https://ytsummary-production.up.railway.app
- **Dockerized Backend**: Fixed port binding ($PORT) and local library path for cloud building.
- **CORS Hardening**: Added environment-based CORS configuration.
- **Root Directory Config**: Configured `/backend` as the build root in Railway.

## [2026-03-24] - Phase 04 Finished ✅
### Added
- **Audio Volume Control**: App Startup now automatically sets Music volume to 80% using Android `AudioManager`.
- **Text-to-Speech (TTS)**: Custom `TtsManager.kt` with markdown cleaning logic to read summaries aloud.
- **Micro-Animations**: Added bounce effect (scale 0.85 -> 1) for the TTS Speaker button in `SummaryScreen`.
- **Navigation Flow**: Connected Main Screen (Link Input) -> Loading -> Summary View via Backend Transcript API.
- **Glassmorphism V2**: Refined `GlassCard` components and added `Pause` icon support via `material-icons-extended`.
- **Phase 04 Expansion**: Added Audio & TTS integration (Auto-volume and TTS Reader).

## [2026-03-24]
### Added
- **Phase 04 Expansion**: Added Audio & TTS integration (Auto-volume and TTS Reader).
- **Glassmorphism UI**: Implemented high-fidelity "Kính mờ" design for Home, Settings, and Summary screens.
- **API Key Management UI**: Added screen to manage, add, and delete Gemini API keys with real persistence.
- **Build Success**: Successfully compiled the Android APK (`app-debug.apk`) for manual testing.
- **Phase 03 Complete**: Ported and verified Key Rotation Logic.
- **Phase 01-02 Complete**: Backend FastAPI and Android Core Infrastructure verified.
- **Transcript Cleaning**: Logic to merge snippets into plain text without timestamps.
- **Multi-Phase Plan**: Created 5 detailed phases in `plans/` directory.
- **Railway Ready**: Added `Dockerfile` and `Procfile` for backend deployment.
- **Local Library Integration**: Successfully integrated `youtube-transcript-api` v1.2.4 to fix XML parsing issues.

### Fixed
- Fixed `ModuleNotFoundError` and environment issues using Python `venv`.
- Fixed `no element found: line 1, column 0` error by upgrading the YouTube transcript library.

### Changed
- Refined architecture: Backend strictly for transcript, Android for AI processing and key rotation.
