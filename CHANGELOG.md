# Changelog - YouTube AI Summarizer (YTSummary)

## [2026-03-24] - Security & Performance Fixes 🛡️⚡
### Fixed
- **Deep Security Filters**: Enforced `BLOCK_ONLY_HIGH` safety settings for Gemini API rather than overriding completely to prevent malicious link generation or Hate Speech.
- **Prompt Injection Defense**: Guarded summary prompt using explicit XML `<transcript>` boundaries and strict "ignore outer instructions" rule to fend off Indirect Prompt Injections.
- **Regex Memory Leak in TTS**: Hoisted Markdown stripping `Regex` instances to `companion object` to mitigate constant re-instantiations and heavy GC pauses during Android TTS.
- **Forced Audio Bug**: Removed intrusive system `setVolume(80)` call from `TtsManager` to honor the device's original user volume state.

## [2026-03-24] - Standalone Final & Cleanup 🧹✅
### Added
- **Gemini 2.5 Flash Integration**: Verified support for `models/gemini-2.5-flash` with thinking-enabled summarization.
- **Python Bridge Verified**: Full end-to-end flow from Kotlin to Local Python (Chaquopy) successfully tested with real YouTube URLs.

### Removed
- **Legacy Cloud Backend**: Deleted `backend/` directory, `Dockerfile`, and `main.py` - the app now runs entirely on-device for transcript fetching.

- **Transcript Helper**: Updated XML parsing logic for compatibility with `yt-transcript-api` master and local `defusedxml`.

### Fixed
- **Chaquopy Build Error**: Resolved `UnknownPluginException` by downgrading from `17.0.1` (unreleased dev version) to `17.0.0` (latest stable).
- **Unknown Python Error / Dependencies Crash**: Explicitly installed transitive dependencies (`requests`, `defusedxml`) in Gradle chaquopy pip block to stop initialization crash.
- **Kotlin Dictionary Mapping Bug**: Replaced `PyObject.get()` (which uses `getattr()`) with `PyObject.asMap()[PyObject.fromJava("key")]` (which invokes python dict `__getitem__`) to successfully extract Python variables.


## [2026-03-24] - Standalone Architecture (Chaquopy Integration) 🚀
### Added
- **Chaquopy**: Integrated `com.chaquo.python` version 17.0.1 for local Python execution on Android.
- **yt_transcript_helper.py**: Ported backend transcript fetching logic to local Python script. Support for `1.2.4+` usage (instance-based `fetch`).
- **PythonManager.kt**: Implemented Kotlin-Python bridge (Singleton) for seamless integration with Android UI.
- **Local Metadata**: Added `oEmbed` metadata fetching via `urllib.request` in Python.
- **End-to-End Success**: Verified full flow (Transcript + Gemini) using a standalone test script on the developer machine.

### Changed
- **SummarizationRepository**: Refactored to use `PythonManager` locally instead of calling the Railway FastAPI backend.
- **Build System**: Updated `gradle/libs.versions.toml`, `build.gradle.kts`, and `settings.gradle.kts` to support external Python dependencies.

## [2026-03-24] - Post-Deployment (Final URL) ✅
### Added
- **Constants.kt**: Created `app/src/main/java/com/skul9x/ytsummary/utils/Constants.kt` for centralized URL management.
- **Production URL Sync**: Updated Android `NetworkModule` to call Railway Production URL (`https://ytsummary-production.up.railway.app/`).
- **Production Stress Test**: Verified production URL with two real-world YouTube videos (Food Chemicals & Healthy Eating).
- **Gemini 2.5 Flash**: Integration verified for summarized content generation via cloud transcript.

### Fixed
- **Railway 502 Error**: Fixed intermittent 502 errors by switching to the official `youtube-transcript-api` and improving port binding using direct python entrypoint.
- **Transcript API Fix**: Corrected `YouTubeTranscriptApi` static method call from `fetch` to `get_transcript` to solve 500/502 errors.
- **Docker Port Binding**: Standardized `Dockerfile` for Railway environment variable injection.


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
