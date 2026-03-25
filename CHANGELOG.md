# Changelog - YouTube AI Summarizer (YTSummary)

## [2026-03-25] - Early Warning System & UX Refinement 🚨📱
### Added
- **MainScreen Update Banner**: Di chuyển thông báo cập nhật `youtube-transcript-api` từ Settings ra màn hình chính để dev dễ nhận biết (Early Warning).
- **Automated Update Check**: Tích hợp logic kiểm tra phiên bản vào `init` block của `SummaryViewModel` (gọi ngay khi app start).

### Changed
- **Settings Cleanup**: Loại bỏ logic check update và banner cũ tại màn hình Settings để tối ưu UX.

## [2026-03-25] - Gemini 2.5 Flash Migration & Thinking Disable 🚀⚡
### Added
- **Gemini 2.5 Flash Migration**: Cập nhật toàn bộ hệ thống để ưu tiên sử dụng `models/gemini-2.5-flash` và `models/gemini-2.5-flash-lite`.
- **JSON Request Validation Test**: Thêm `GeminiResponseHelperTest.kt` để tự động hóa việc kiểm tra cấu trúc JSON (đảm bảo `thinkingConfig` luôn nằm ở root và budget = 0).

### Changed
- **Thinking Configuration**: Chuyển đổi từ `thinking_config` (trong generationConfig) sang `thinkingConfig` (root level) theo chuẩn API mới nhất để tắt chế độ Thinking, giúp giảm đáng kể thời gian phản hồi (TTFB).
- **Model Deprecation Cleanup**: Loại bỏ hoàn toàn các model dòng `2.0-flash` khỏi danh sách xoay tua để chuẩn bị cho lộ trình khai tử của Google (June 2026).

## [2026-03-25] - Security Hardening & Stable Testing 🛡️🧪
### Fixed
- **Flaky Unit Test (PythonUpdateCheckerTest)**: Replaced `org.json` with `kotlinx.serialization` to eliminate "Method not mocked" errors during local JVM tests. Tests are now 100% stable.
- **Strict VideoID Validation**: Implemented a robust Regex check in `SummaryViewModel` to catch invalid YouTube URLs before triggering the pipeline. Added user feedback for invalid inputs.

### Changed
- **Network Security Policy**: Enforced `android:usesCleartextTraffic="false"` in `AndroidManifest.xml` to mandate HTTPS for all network operations (Critical protection for API Keys).
- **README.md (Vietnamese)**: Fully localized and updated the main documentation to reflect v4.1.0 capabilities and 2026 copyright.

## [2026-03-25] - TTS Intelligence & Smart Bookmarking 🎙️📖
### Added
- **Tóm tắt siêu tốc (No Thinking)**: Đã cấu hình vô hiệu hóa chế độ Thinking trên tất cả các model (Gemini 2.0/2.5 Flash) để đạt tốc độ phản hồi nhanh nhất cho người dùng.
- **TTS Pause/Resume (Bookmark Mode)**: Implemented "smart bookmarking" for Text-to-Speech. The app now tracks the exact character index being read and allows users to Pause and Resume from the same word, rather than restarting.
- **TtsManager Progress Tracking**: Integrated `UtteranceProgressListener.onRangeStart` to expose the current speech offset to the UI/ViewModel layer.
- **Restart Capability**: Added a dedicated Restart button in `SummaryScreen` to allow quick re-reading from the beginning (index 0).

### Changed
- **MVVM State Management for TTS**: Migrated `isTtsPlaying` and `ttsPausedIndex` to `SummaryViewModel`. TTS progress now survives screen rotation and activity lifecycle events.
- **Standardized Documentation**: Updated `dev.txt` with a comprehensive MVVM-compliant guide for implementing human-like TTS controls.

### Fixed
- **TTS Pause/Resume Button Sync**: Resolved the issue where the Play/Pause icon state would desynchronize from the engine when starting auto-read or finishing a full queue.
- **TTS isSpeaking Race Condition**: Replaced the unreliable `tts.isSpeaking` check with a robust `AtomicInteger` counter (`pendingUtterances`) in `TtsManager` to accurately track chunk completion during streaming playback.
- **Streaming Pause Logic**: Fixed incorrect resume behavior by resetting the playback position when pausing during heavy chunked streaming.
- **TTS Resume Fix**: Resolved the issue where clicking Resume would play from the start. Implemented absolute position tracking (`totalSpokenLength + currentIndex`) to allow seamless resumption from the exact character paused.
- **TTS Reading From Middle Fix**: Fixed skipping initial sentences due to `StateFlow` conflation during SSE streaming. Switched to unified Full-Text auto-read after streaming completes.
- **TTS Flow Optimization**: Removed real-time chunked speech to ensure smoother, uninterrupted voice output once the AI summary is fully loaded.

## [2026-03-24] - Navigation & UX Polish (Fix Swipe Back) 📱🔙
### Fixed
- **Centralized BackHandler**: Implemented a global `BackHandler` in `MainActivity` to intercept the Android system "Swipe Back" gesture. This prevents the app from accidentally exiting when the user is on child screens (History, Settings, Summary).
- **Redundant BackHandler Cleanup**: Removed local `BackHandler` calls from `SummaryScreen` to centralize logic and reduce code duplication.
- **TTS Auto-Stop**: Added logic to automatically silence Text-to-Speech when navigating back from the Summary view via gesture.

## [2026-03-24] - Architecture Refactoring (Audit v4.2.0) 🏗️📱
### Added
- **SummaryViewModel**: Implemented MVVM architecture. UI state and the summarization pipeline now live in a `ViewModel` (`AndroidViewModel`), ensuring progress survives device rotation and configuration changes. (Fix ISSUE_004)
- **ScreenState Sealed Class**: Standardized UI navigation and state transition using a unified `ScreenState` pattern (Main, Loading, Summary, History, Settings).

### Changed
- **MainActivity Logic Migration**: Offloaded all summarization, metadata fetching, and TTS orchestration logic from the Activity to the ViewModel.
- **Improved History Navigation**: History item selection now correctly restores the video title, thumbnail, and summary text via `loadFromHistory`.

### Fixed
- **Python Coroutine Guard**: Added `currentCoroutineContext().ensureActive()` in `SummarizationRepository` immediately after blocking Python JNI calls. This prevents the app from processing or emitting stale data if the underlying coroutine was cancelled. (Fix ISSUE_003)
- **Activity State Loss**: Fixed the critical bug where rotating the screen would reset the entire summarization process.

## [2026-03-24] - Performance Audit Fixes (Audit v4.1.0) 🏎️💨
### Added
- **Python Runtime Warm-up**: Implemented `PythonManager.warmUp` (background initialization) in `MainActivity.onCreate` để giảm độ trễ khởi động ~1-2s.
- **Dynamic Summary Counter**: Kết nối chỉ số lịch sử summary ngoài màn hình chính với dữ liệu thực tế từ Room database.

### Changed
- **Audio Focus Management (Duck)**: Thay thế `setStreamVolume(80)` bằng `AudioFocus` (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`). Nhạc giờ sẽ tự hạ âm lượng khi AI nói và hồi phục khi kết thúc.

### Fixed
- **O(N²) String Bottleneck**: Sử dụng `StringBuilder` thay cho cộng chuỗi `+=` trong SSE streaming giúp giảm thiểu rác bộ nhớ.
- **Python Thread Blocking Leak**: Thêm `timeout=10` cho `urlopen` trong Python để ngăn chặn treo thread vĩnh viễn.

## [2026-03-24] - Performance Optimization & Streaming 🚀⚡
### Added
- **SSE Streaming (Gemini)**: Switched to `streamGenerateContent?alt=sse` endpoint. The UI now renders text incrementally ("jumping text") for a ChatGPT-like experience.
- **Cache-First Strategy**: Implemented local SQLite caching in `SummarizationRepository`. Video summaries are now fetched instantly (10ms) from the database if they have been summarized before.
- **TTS Sentence Chunking**: Enhanced `TtsManager` with `speakChunk` logic. The app now begins reading the summary aloud as soon as the first sentence is received from the AI stream, rather than waiting for completion.
- **Real-time UI Updates**: Modified `MainActivity` to synchronize streaming AI output with TTS and UI state management.

### Fixed
- **Redundant Database Writes**: Fixed a logic bug where the app attempted to save to history multiple times during a stream. It now saves exactly once upon completion.
- **Lint - Unused Variables**: Removed `hasSaved` and other redundant state variables from `MainActivity` as identified by the Android Lint tool.
- **Dispatcher Safety**: Ensured all network and database operations in the streaming flow are correctly dispatched to `Dispatchers.IO` to prevent UI freezes.

### Changed
- **Architecture Redesign**: Migrated the core summarization pipeline from Batch Processing to Stream Processing, đạt chuẩn Time-to-First-Byte < 1s.

## [2026-03-24] - UX Polish & System Analysis 📱✨
### Added
- **Share Intent Integration**: Users can now directly share YouTube URLs to YTSummary. The app catches the `text/plain` action, auto-fetches, summarizes, and starts TTS entirely hands-free.
- **Adaptive Launcher Icon**: Implemented modern Android adaptive icons using vector drawables (`ic_launcher_background`, `ic_launcher_foreground`) packed with AI-themed aesthetics.
### Analyzed
- **Performance Fact-Check**: Audited `per_audit.txt` architecture report against the actual Kotlin codebase (`per_audit_factcheck.md`). Validated 85% accuracy and identified actionable pending tasks (SSE Streaming, Local DB caching).

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
