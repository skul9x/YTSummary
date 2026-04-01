---
title: Migrate from Python youtube-transcript-api to Native Kotlin
status: draft
created: 2026-03-31
owner: dev
priority: high
estimate: 2-3 weeks
---

## 🎯 Objective

Eliminate Python/Chaquopy dependency by replacing `youtube-transcript-api` with a native Kotlin/Java solution, reducing APK size by ~7-10MB and removing Python runtime overhead.

---

## 📊 Current State Analysis

### Architecture
```
Kotlin UI → PythonManager (Chaquopy) → yt_transcript_helper.py → youtube-transcript-api → YouTube
```

### Key Components
1. **PythonManager.kt** - Bridge using Chaquopy (lines 1-116)
2. **yt_transcript_helper.py** - Python wrapper with two functions:
   - `get_transcript(video_id)` - fetches transcript with language fallback (vi, en)
   - `get_metadata(video_id)` - fetches metadata via YouTube oEmbed API (already HTTP-based)
3. **Dependencies**:
   - Chaquopy plugin + Python 3.12 runtime (~7-10MB APK impact)
   - Python packages: `youtube-transcript-api`, `requests`, `defusedxml`
4. **Caching**: `TranscriptCache` saves transcripts to file system (24h TTL)
5. **Update Mechanism**: `PythonUpdateChecker` monitors PyPI for library updates

### Functional Requirements
- ✅ Fetch transcript with language priority: Vietnamese → English
- ✅ Handle errors: `TranscriptsDisabled`, `NoTranscriptFound`, `VideoUnavailable`, IP blocking
- ✅ Parse transcript data (snippets with text + timestamps)
- ✅ Return clean plain text (timestamps removed)
- ✅ Fetch metadata (title, thumbnail, author) - already works via oEmbed
- ✅ Maintain transcript cache (24h TTL)
- ✅ Support for both manual and auto-generated captions

---

## 🔍 Research Phase (Days 1-2)

### 2.1 Library Evaluation

**Primary Option: Java Port**
- Library: `com.github.kiwiz:youtube-transcript-api` (active maintenance, MIT license)
- Check: Android compatibility, API similarity to Python version, error handling, ProGuard rules
- Test: Simple proof-of-concept to fetch a transcript

**Secondary Option: YouTube Data API v3**
- Use Google's official Captions API (requires API key, separate quota)
- Pros: Official, stable
- Cons:
  - Doesn't provide transcripts directly (only caption tracks in XML/JSON)
  - Requires conversion from caption format to plain text
  - Limited quota (separate from Gemini)
  - More complex: need to list captions → download → parse
  - Some captions require OAuth for private videos

**Tertiary Option: Direct HTTP Implementation**
- Reverse-engineer YouTube's internal transcript endpoints
- Pros: No external dependencies, full control
- Cons: Very brittle, breaks when YouTube changes, needs signature deciphering for some videos

### Decision Criteria
1. ✅ Works on Android (minSdk 26)
2. ✅ Handles language fallback (vi, en)
3. ✅ Same error types as Python version
4. ✅ No additional API quota requirements
5. ✅ Minimal APK size increase
6. ✅ MIT/Apache 2.0 license

### 2.2 Proof of Concept

Create sample app module to test:
```kotlin
// Test with popular videos
val transcript = YouTubeTranscriptApi.getTranscript("dQw4w9WgXcQ", listOf("vi", "en"))
```

Verify:
- Vietnamese transcripts work
- Fallback to English works
- Error cases match Python behavior
- Performance (cold/hot)

---

## 🏗️ Design Phase (Day 3)

### 3.1 Interface Design

Create abstraction layer for future flexibility:

```kotlin
interface TranscriptService {
    suspend fun fetchTranscript(videoId: String): Result<String>
    suspend fun fetchMetadata(videoId: String): Result<VideoMetadata>
    suspend fun healthCheck(): Boolean
}

// Current implementation: NativeTranscriptService
// Future could have: InvidiousTranscriptService, HybridService
```

### 3.2 Error Handling Mapping

Map Python exceptions → Kotlin sealed class:

| Python Exception | Kotlin Type |
|------------------|-------------|
| TranscriptsDisabled | TranscriptUnavailableException("Video này đã tắt phụ đề.") |
| NoTranscriptFound | TranscriptUnavailableException("Không tìm thấy phụ đề phù hợp (vi/en).") |
| VideoUnavailable | VideoUnavailableException |
| CouldNotRetrieveTranscript / IPBlocked | TranscriptFetchException("YouTube đang chặn request.") |
| HTTP errors | NetworkException |

### 3.3 Caching Strategy

Keep existing `TranscriptCache.kt` - no changes needed. It uses file system with 24h TTL.

### 3.4 Package Structure

```
app/src/main/java/com/skul9x/ytsummary/
├── service/
│   ├── TranscriptService.kt (interface)
│   ├── NativeTranscriptService.kt (implementation)
│   ├── exceptions/
│   │   ├── TranscriptException.kt
│   │   ├── TranscriptUnavailableException.kt
│   │   ├── VideoUnavailableException.kt
│   │   └── NetworkException.kt
│   └── models/
│       ├── TranscriptSnippet.kt (data class for snippet with text, start, duration)
│       └── VideoMetadata.kt (already exists)
```

---

## 🛠️ Implementation Phase (Days 4-10)

### 4.1 Gradle Setup (Day 4)

**Remove Chaquopy:**
```diff
plugins {
-   alias(libs.plugins.chaquopy)
}

chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install("youtube-transcript-api")
            install("requests")
            install("defusedxml")
        }
    }
}
```

**Add transcript library:**
```kotlin
dependencies {
    implementation("com.github.kiwiz:youtube-transcript-api:VERSION_TO_BE_DETERMINED")
}
```

**Update `libs.versions.toml`:**
```toml
youtubeTranscript = "x.y.z"  # latest stable
```

### 4.2 Exception Types (Day 5)

Create sealed hierarchy for transcript errors:

```kotlin
sealed class TranscriptException(message: String) : Exception(message)
data class TranscriptUnavailableException(val reason: String) : TranscriptException(reason)
class VideoUnavailableException : TranscriptException("Video không tồn tại hoặc bị ẩn.")
class TranscriptFetchException(message: String) : TranscriptException(message)
class NetworkException(message: String) : TranscriptException(message)
```

### 4.3 NativeTranscriptService (Day 6-7)

Implement using chosen library:

```kotlin
class NativeTranscriptService @Inject constructor(
    private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TranscriptService {

    override suspend fun fetchTranscript(videoId: String): Result<String> = withContext(dispatcher) {
        try {
            // Use library to fetch transcript with language priority
            val transcriptList = YouTubeTranscriptApi.getTranscript(
                videoId,
                listOf("vi", "en")
            )

            // Convert to plain text (library may return list of snippets)
            val plainText = transcriptList.joinToString(" ") { it.text }
                .replace("\n", " ")
                .trim()

            Result.success(plainText)
        } catch (e: TranscriptsDisabled) {
            Result.failure(TranscriptUnavailableException("Video này đã tắt phụ đề."))
        } catch (e: NoTranscriptFound) {
            Result.failure(TranscriptUnavailableException("Không tìm thấy phụ đề phù hợp (vi/en)."))
        } catch (e: VideoUnavailable) {
            Result.failure(VideoUnavailableException())
        } catch (e: CouldNotRetrieveTranscript) {
            Result.failure(TranscriptFetchException("YouTube đang chặn request. Thử lại sau."))
        } catch (e: IOException) {
            Result.failure(NetworkException("Lỗi mạng: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(TranscriptFetchException("Lỗi không xác định: ${e.message}"))
        }
    }

    override suspend fun fetchMetadata(videoId: String): Result<VideoMetadata> {
        // Keep existing oEmbed implementation (already native HTTP)
        // Or switch to YouTube Data API if desired
        return fetchMetadataViaOEmbed(videoId)
    }

    override suspend fun healthCheck(): Boolean = try {
        YouTubeTranscriptApi.getTranscript("dQw4w9WgXcQ", listOf("en"))
        true
    } catch (e: Exception) {
        false
    }
}
```

### 4.4 Update PythonManager → TranscriptManager (Day 8)

Refactor repository to use new service:

```kotlin
// Update SummarizationRepository.kt
class SummarizationRepository private constructor(context: Context) {
    private val transcriptService: TranscriptService = NativeTranscriptService.getInstance(context)
    // Remove: private val pythonManager

    fun getSummary(videoId: String): Flow<AiResult> = flow {
        // Check cache first (unchanged)
        val cached = summaryDao.getSummaryById(videoId)
        if (cached != null) {
            emit(AiResult.Success(cached.summaryText, "cache"))
            return@flow
        }

        // Fetch transcript via native service
        var transcript = transcriptCache.get(videoId)

        if (transcript == null) {
            emit(AiResult.Loading("📺 Đang lọc phụ đề..."))
            val transcriptResult = transcriptService.fetchTranscript(videoId)

            if (transcriptResult.isFailure) {
                emit(AiResult.Error("Lỗi lấy phụ đề: ${transcriptResult.exceptionOrNull()?.message}"))
                return@flow
            }

            transcript = transcriptResult.getOrNull()
            if (transcript.isNullOrBlank()) {
                emit(AiResult.Error("Không lấy được nội dung phụ đề"))
                return@flow
            }

            transcriptCache.save(videoId, transcript)
        }

        // Rest unchanged...
    }.flowOn(Dispatchers.IO)
}
```

### 4.5 Remove Python-Specific Code (Day 9)

- Delete `app/src/main/python/` directory
- Delete `PythonManager.kt`
- Delete `PythonUpdateChecker.kt` (or replace with library version check if needed)
- Remove Chaquopy plugin from `build.gradle.kts`
- Update `YTSummaryApplication.kt` if it references Python
- Remove warm-up logic (Python warm-up)

**Note**: Keep TranscriptCache.kt and all caching logic - no changes needed.

### 4.6 Update ViewModels & UI (Day 10)

Check if any UI code directly references Python:
- `SummaryViewModel.kt` - should only use Repository, no direct changes needed
- `HistoryScreen.kt`, `MainActivity.kt` - no changes

Verify all error messages still meaningful in Vietnamese.

---

## 🧪 Testing Phase (Days 11-14)

### 5.1 Unit Tests

Update existing tests in `app/src/test/java/com/skul9x/ytsummary/`:
- `SummarizationRepositoryTest.kt` - mock `TranscriptService` instead of `PythonManager`
- New: `NativeTranscriptServiceTest.kt` - test error handling, parsing
- Test language fallback: ensure Vietnamese preferred over English

**Test Scenarios:**
```kotlin
@Test
fun `vi transcript returned when available`() = runTest {
    // Mock or use real video with Vietnamese captions
    val result = service.fetchTranscript("video_with_vi")
    assertTrue(result.isSuccess)
    assertTrue(result.getOrNull()!!.length > 100)
}

@Test
fun `fallback to en when vi not available`() = runTest {
    val result = service.fetchTranscript("video_only_en")
    assertTrue(result.isSuccess)
}

@Test
fun `error when transcripts disabled`() = runTest {
    val result = service.fetchTranscript("video_disabled_transcripts")
    assertTrue(result.exceptionOrNull() is TranscriptUnavailableException)
}

@Test
fun `error when video unavailable`() = runTest {
    val result = service.fetchTranscript("invalid_id")
    assertTrue(result.exceptionOrNull() is VideoUnavailableException)
}

@Test
fun `handle IP blocking gracefully`() = runTest {
    // Simulate or test with known blocked video
}
```

### 5.2 Integration Tests

Create real integration tests with YouTube videos:
- Use small, stable videos (e.g., official music videos, educational content)
- Test batch of 10-20 videos covering different scenarios
- Compare output with Python version (same transcript text?)
- Measure performance (cold/warm start, caching)

**Sample videos to test:**
- Video with both vi & en captions: `dQw4w9WgXcQ` (Richie - classic test)
- Video with only English
- Video with auto-generated captions only
- Video with disabled transcripts
- Non-existent video ID

### 5.3 APK Size Measurement

Build debug/release APK **before** and **after**:
- Report size difference
- Expected: -7 to -10 MB (Python runtime + dependencies)

### 5.4 Performance Benchmarking

- Cold start time (first transcript fetch)
- Warm start time (cached transcript)
- Memory usage comparison
- Battery impact (if possible)

---

## 📋 Migration Checklist

### Pre-Migration (Safety Net)
- [ ] Create backup branch: `git checkout -b migrate/youtube-transcript-native`
- [ ] Ensure all tests passing on Python version
- [ ] Document current APK size, performance metrics
- [ ] Archive Python script files in case rollback needed

### Code Changes
- [ ] Add `youtube-transcript-api` Java library dependency
- [ ] Create `TranscriptService` interface
- [ ] Implement `NativeTranscriptService`
- [ ] Create exception classes
- [ ] Refactor `SummarizationRepository` to use new service
- [ ] Remove `PythonManager.kt`
- [ ] Delete `app/src/main/python/` directory
- [ ] Remove Chaquopy plugin and config from `build.gradle.kts`
- [ ] Remove Python dependencies (youtube-transcript-api, requests, defusedxml)
- [ ] Remove `PythonUpdateChecker.kt` or replace with Maven version check
- [ ] Remove any Python warm-up calls from `YTSummaryApplication`
- [ ] Update imports in affected files

### Testing
- [ ] Run unit tests - all passing
- [ ] Run integration tests with real YouTube videos
- [ ] Manual testing on physical device:
  - Fetch transcript with vi/vi+en priority
  - Test fallback scenarios
  - Verify caching works (transcripts saved to disk)
  - Check error messages appear correctly
  - Verify metadata fetching (title, thumbnail) works
- [ ] Performance testing meets thresholds:
  - First fetch: < 5s
  - Cached fetch: < 500ms
- [ ] APK size reduced by 7+ MB

### Documentation
- [ ] Update `README.md` - remove Python/Chaquopy mention
- [ ] Update `system_overview.md` - reflect native transcript service
- [ ] Update `CHANGELOG.md` - entry for migration
- [ ] Update developer docs on transcript service architecture

### Post-Migration Validation
- [ ] Compare transcript quality between Python and Kotlin versions (text identical?)
- [ ] Verify no increase in error rate (check logs)
- [ ] Ensure update mechanism works (if kept) - check for library updates
- [ ] Submit to internal testing (Alpha track)

---

## ⚠️ Risks & Mitigations

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| YouTube changes API endpoints | Medium | High | Choose library with active maintenance; have fallback to Python if urgent fix needed |
| Java library lacks some Python features | Low | Medium | Prototype first; can extend library or add hybrid approach |
| APK size not significantly reduced | Low | Low | Still worth it for removing Python complexity |
| Legal/ToS violations from scraping | Low | High | Both Python and Java approaches use same method - no increased risk; ensure use for personal/educational purposes only |
| Performance degradation | Medium | Medium | Benchmark thoroughly; optimize if needed (connection pooling, caching) |
| Language fallback fails | Low | Medium | Test with diverse video set; implement custom fallback logic if needed |

---

## 📈 Success Metrics

1. ✅ **APK size**: Reduced by minimum 7MB (Python runtime)
2. ✅ **Performance**: Cold start < 5s, warm start < 500ms (same or better)
3. ✅ **Test coverage**: 100% of error scenarios tested
4. ✅ **Functional parity**: All YouTube videos that worked before still work
5. ✅ **No regressions**: Error rate < 1% increase (if any)
6. ✅ **Code quality**: Clean architecture, proper abstraction, documented

---

## 🔄 Rollback Plan

If critical issues arise:

1. **Immediate rollback** (within 1 hour):
   ```bash
   git checkout main  # or previous stable tag
   # Rebuild with Chaquopy
   ```

2. **Partial rollback** if only edge cases fail:
   - Keep native service but add Python fallback for error cases
   - Use circuit breaker pattern: try native 3 times, fall back to Python

3. **Communication**:
   - Inform testers of rollback
   - Log issue with detailed stack traces
   - Create GitHub issue for tracking

---

## 📚 References

- Python library: https://github.com/jdepoix/youtube-transcript-api
- Java port candidate: https://github.com/kiwiz/youtube-transcript-api
- YouTube Data API v3: https://developers.google.com/youtube/v3/docs/captions
- Chaquopy documentation: https://chaquo.com/chaquopy/
- Current architecture: `docs/architecture/system_overview.md`

---

## 🗓️ Timeline Estimate

| Phase | Duration | Days |
|-------|----------|------|
| Research & POC | 2 days | 1-2 |
| Design | 1 day | 3 |
| Implementation | 5 days | 4-8 |
| Testing | 4 days | 9-12 |
| Validation & Bug Fixes | 2 days | 13-14 |
| Documentation | 1 day | 14 |
| **Total** | **~12-15 days** | |

**Buffer**: Add 3-5 days for unforeseen issues → **2-3 weeks total**

---

## 🎯 Optional Enhancements (After Migration)

Once native migration is stable:

1. **Multi-source transcripts**:
   - Add Invidious/Piped API as fallback (privacy-focused, avoids YouTube directly)
   - Hybrid approach: try YouTube first, fall back to Invidious if blocked

2. **Smart retry logic**:
   - Exponential backoff per-language
   - Proxy rotation if IP blocked (advanced)

3. **Update checker**:
   - Monitor Maven Central for new versions of transcript library
   - Show in-app update banner like current Python version check

4. **Analytics**:
   - Track success rate by language, video type
   - Monitor blocking incidents

5. **Pre-fetch transcripts**:
   - If user watches video in YouTube app, can pre-download transcript
   - Background sync for subscribed channels

---

## 🔧 Tools & Dependencies

### To Add
```gradle
implementation("com.github.kiwiz:youtube-transcript-api:1.x.x")  // TBD
```

### To Remove
```gradle
chaquopy {}
// Python packages: youtube-transcript-api, requests, defusedxml
```

### To Keep (unchanged)
- OkHttp (already in project)
- Kotlinx Serialization (already in project)
- TranscriptCache (file system)
- Room/SQLCipher (for summary history)

---

## ❓ Open Questions

1. **Library version**: Which Java port is most actively maintained? Need to check GitHub repos.
2. **License compatibility**: MIT/Apache 2.0?
3. **ProGuard rules**: Does the library need special rules? Check documentation.
4. **Language detection**: Does the Java library auto-detect or require explicit language list? Python version uses `languages=['vi', 'en']`.
5. **Metadata approach**: Keep oEmbed or switch to YouTube Data API? (oEmbed is simpler, no API key)
6. **Error message consistency**: Ensure user-facing Vietnamese messages match current ones exactly.

---

## ✅ Migration Complete Criteria

- [ ] All tests passing (unit + integration)
- [ ] Manual QA completed on 50+ diverse YouTube videos
- [ ] APK built and installed successfully
- [ ] TranscriptCache working (files saved to disk)
- [ ] No Python references in codebase
- [ ] Documentation updated
- [ ] Release notes prepared
- [ ] Rollback plan documented and tested

---

**Status**: Ready for review and execution
**Next step**: Approval to begin Day 1-2 research and proof-of-concept
