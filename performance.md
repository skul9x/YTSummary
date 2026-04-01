# YTSummary Android Performance Audit

Date: 2026-03-31
Scope: Static code and configuration audit of runtime performance, UI smoothness, memory efficiency, network efficiency, and scalability.
Method: Source-level analysis (no runtime profiler trace attached in this audit).

## Executive Summary

YTSummary already applies several good performance patterns (IO dispatching, Paging 3 for history, transcript caching, non-blocking retry with coroutine delay, summary text chunk rendering, and startup macrobenchmark scaffolding). However, the current architecture still has a few high-impact hotspots that can cause jank, excessive CPU/GPU work, and avoidable memory/network overhead under long transcript and streaming workloads.

Top findings:
- Highest impact UI bottleneck: streaming emits full accumulated text on every SSE chunk; UI state updates trigger large recompositions and repeated text chunk list rebuilds.
- Highest impact data bottleneck: history list query loads full summary payload for every row, increasing DB I/O and memory pressure.
- Highest impact network risk: response resource handling is inconsistent in some transcript fetch paths (missing use blocks), with leak risk under error branches.
- Startup optimization is partially implemented, but Baseline Profile generation/integration is incomplete in build setup (macrobenchmark exists, profile generation pipeline not fully wired).
- Architecture documentation and actual runtime stack diverge in key places (Retrofit/Hilt claims vs direct OkHttp/manual singletons), which can hide optimization opportunities and complicate maintenance.

If you implement only the first 5 quick wins in this report, expected gains are:
- Lower recomposition cost during live SSE rendering.
- Smoother scrolling in summary and history views.
- Reduced GC churn and lower peak heap for long outputs.
- Improved resilience under unstable network conditions.
- Better cold-start consistency after profile integration.

## Architecture Overview

Current practical architecture:
- UI Layer: Jetpack Compose + AndroidViewModel + StateFlow.
- Domain/Repository Layer: SummarizationRepository orchestrates metadata, transcript fetch, SSE summarize, and persistence.
- Data Layer: OkHttp-based network calls (Gemini + InnerTube + oEmbed), Room + SQLCipher database, file-based transcript cache.

Observed implementation characteristics:
- Dependency injection is mostly manual singleton/factory style. Hilt is not actually wired in current app runtime path.
- Network stack is direct OkHttp usage; Retrofit dependencies exist in catalog but are not used for active API paths.
- Concurrency model is coroutine Flow with explicit Dispatchers.IO in repositories/services.

Performance implication:
- This architecture can perform well, but state fan-out and coarse-grained UI state updates currently dominate runtime overhead during streaming.

## UI Rendering Performance Analysis

Strengths:
- Summary view uses LazyColumn rather than a giant Column.
- Summary text is pre-chunked for rendering.
- Paging 3 is used for history screen virtualization.

Risks and inefficiencies:
- Main composition scope is broad: multiple collectAsState calls and screen switching are centralized in MainActivity content tree, so frequent stream updates can recompose more UI than necessary.
- During streaming, each update includes full accumulated summary text, causing large string propagation through ScreenState and repeated rendering work.
- History rows render thumbnails via AsyncImage in a dense list. No explicit sizing policy issue, but repeated list invalidation can still increase work when DB updates happen.

Recommendations:
- Split state ownership per screen composable and reduce root-level state collection breadth.
- Isolate streaming text rendering into a dedicated sub-composable with stable parameters.
- Use immutable UI state data classes with finer-grained state fields (stream text, loading message, metadata, playback state) instead of replacing full screen state object per event.

## Jetpack Compose Recomposition Analysis

Major hotspot:
- GeminiApiClient emits AiResult.Success(accumulatedText.toString(), model) per chunk.
- SummaryViewModel writes each emission to _screenState.
- SummaryScreen recalculates chunk list whenever summaryText changes (remember key changes every emission), then LazyColumn reprocesses items.

Impact profile:
- Time complexity behavior can approach O(n^2) across a long stream because total text length increases and each emission rebuilds large structures.
- Frequent state writes on main thread can cause frame pacing instability and jank.

Recommended optimization pattern:
- Keep streaming as incremental deltas, not full concatenated text at each event.
- In ViewModel, buffer chunks and publish UI updates at a fixed cadence (for example every 50-120 ms) to align with frame budget.
- In Compose, keep a SnapshotStateList of chunks (append only) instead of rebuilding whole chunk list from one giant string.
- Use derivedStateOf for computed display fragments and keep expensive text transforms off composition path.

## Coroutine and Flow Concurrency Analysis

Strengths:
- Network/database/transcript operations are mostly off main thread via Dispatchers.IO.
- retryWithBackoff uses suspend delay (non-blocking).

Risks:
- Double dispatcher wrapping in some areas (withContext(IO) plus upstream flowOn(IO)) adds complexity and can obscure context ownership.
- Stream collection writes to MutableStateFlow on every SSE chunk; this can flood UI with high-frequency emissions.
- Metadata flow is collected in parallel async block and joined only at persistence point; acceptable, but lifecycle cancellation handling should be explicit for robustness.

Recommendations:
- Standardize dispatcher ownership: service methods return suspend/Flow without nested withContext unless needed at I/O boundary.
- Add Flow operators for stream shaping in ViewModel: buffer, conflate, sample, or custom frame-clock throttling.
- Consider distinctUntilChanged or minimum text delta threshold before UI emission.

## Streaming Performance (Gemini SSE)

Current behavior:
- SSE loop reads line by line and emits full accumulated text whenever a data chunk contains text.

Bottlenecks:
- No backpressure strategy between network read speed and UI render speed.
- Full-string re-emission amplifies CPU allocations and GC pressure.
- Continuous conversion to String on each append increases allocation churn.

Optimization strategy:
- Emit semantic chunk events (append text chunk) rather than full current buffer.
- Add coalescing window in ViewModel (for example 80 ms) before pushing UI state.
- Keep StringBuilder in ViewModel/repository and only materialize full string for final persistence or on-demand copy/share.
- Consider parsing SSE on a dedicated buffered channel and consume at UI-safe cadence.

## Network Layer Performance

Strengths:
- Shared connection pool and cache-enabled OkHttp base client.
- Separate read timeout for Gemini streaming.
- Retry logic is coroutine-based and non-blocking.

Issues:
- Logging interceptor at BODY in debug can be very expensive for large transcript/stream payloads.
- Retry currently focuses mostly on IOException in Gemini path; retry policy can be expanded for transient HTTP classes where safe.
- Response resource lifecycle handling is inconsistent in some transcript methods (not all calls are enclosed with use), which can leak sockets under exceptions.

Recommendations:
- Restrict debug logging to HEADERS or BASIC for heavy endpoints (SSE/transcript).
- Ensure all execute() responses are always wrapped with use blocks.
- Add event listener metrics (DNS/connect/TLS/request/response durations) for real latency telemetry.
- Evaluate separate OkHttp instance tuning for transcript burst workloads (maxRequestsPerHost, dispatcher limits).

## Database Performance (Room)

Strengths:
- PagingSource query for history.
- Useful indices on videoId and timestamp.

Main bottleneck:
- History list query uses SELECT * and includes summaryText (potentially large), even though list screen only needs title/thumbnail/timestamp/videoId.

Impact:
- Increased disk I/O, cursor window pressure, object allocation, and memory retention during list scroll.

Recommendations:
- Introduce lightweight projection entity/DTO for history rows.
- Keep full summaryText fetch only for detail/open action.
- Consider FTS or precomputed preview column for quick snippets.
- Add pragma/wal verification and DB inspector benchmark for large datasets.

## Transcript Parsing Performance

Strengths:
- XmlPullParser (streaming parser) is appropriate for large XML.
- Text cleaning avoids heavyweight parser dependencies.

Hotspots:
- XmlPullParserFactory.newInstance created on every parse.
- Multiple string replacements per snippet can become CPU-heavy for very large transcripts.
- InnerTube flow performs multiple full response body string materializations.

Recommendations:
- Reuse parser factory instance where safe.
- Micro-optimize cleanTranscriptText with early exits (already partially done) and reduce chained allocations.
- If transcript sizes continue to grow, benchmark parser and consider pooled buffers.

## Memory Usage Analysis

Observed pressure sources:
- Repeated full-string growth and emission during SSE.
- Recursive TTS chunking enqueues many utterances for very long text and stores per-utterance offset map entries.
- History list currently hydrates full summary payload.

Recommendations:
- Switch to incremental streaming model to avoid repeated full-string copies.
- Convert recursive TTS chunk queueing to iterative producer with bounded queue size.
- Add memory benchmarks for long summary sessions (10k-50k words).
- Add LeakCanary in debug builds to detect retained activity/composable references.

## Cold Start and App Startup Performance

Current status:
- Macrobenchmark startup test exists (cold startup timing metric).
- profileinstaller dependency exists.
- No generated baseline profile artifacts found in repository and no clear profile producer module/plugin wiring.

Likely startup constraints:
- MainActivity initializes TTS manager and notification channel in onCreate.
- ViewModel pre-warms Room in background; useful, but still contributes startup workload if triggered too early in first frame lifecycle.

Recommendations:
- Complete Baseline Profile generation pipeline and include generated profile artifacts in release builds.
- Defer non-critical startup work until after first frame (for example post-frame init for TTS if not immediately required).
- Add StartupTracing + Macrobenchmark for TTID/TTFD metrics, not only startup total.

## Main Bottlenecks Identified

1. High-frequency full-text UI updates from SSE causing heavy recomposition and allocation churn.
2. History query over-fetching large summaryText in list path.
3. Missing uniform response use blocks in transcript networking path.
4. Incomplete baseline profile integration despite benchmark scaffolding.
5. Broad root-level Compose state collection causing avoidable recomposition scope.

## Quick Wins (Low Effort / High Impact)

1. Throttle streaming UI state updates to fixed cadence (50-120 ms).
2. Emit incremental text chunks instead of full accumulated string each SSE event.
3. Add lightweight Room projection for history list rows.
4. Wrap all OkHttp execute responses in use blocks.
5. Reduce logging interceptor verbosity for large payload endpoints.
6. Isolate streaming text composable and avoid recomposing full screen on each chunk.
7. Replace recursive TTS chunk scheduling with iterative bounded approach.
8. Defer non-critical startup initializations until after first frame.

## Advanced Optimization Strategies

- Introduce a dedicated StreamRenderState:
  - chunkList: SnapshotStateList<String>
  - finalText: String materialized only on completion
  - uiTick: frame-aligned update signal
- Use channelFlow or SharedFlow with replay=0 and extraBufferCapacity for stream event ingestion, then sample on UI.
- Add structured performance telemetry:
  - per-phase latency: transcript fetch, parse, gemini first-token, full completion, DB save
  - frame metrics during streaming screens
  - memory snapshots pre/post long summary
- Consider migrating network APIs to Retrofit only if it improves maintainability and instrumentation consistency; performance benefit alone is usually neutral vs optimized OkHttp direct usage.

## Android Best Practice Recommendations

- Keep composables small and state-hoisted with stable parameter contracts.
- Use immutable UI models and avoid passing giant mutable strings through top-level state repeatedly.
- Use Room projections and paging-specific entities for list pages.
- Validate baseline profile in CI with macrobenchmark thresholds.
- Keep debug-only expensive diagnostics gated (network body logs, verbose tracing).
- Prefer Hilt or a consistent DI approach for lifecycle-safe singleton management and testability.

## Scalability Considerations

As user volume and data size grow:
- Long transcript + long summary scenarios will magnify current O(n^2)-like UI update behavior unless incremental rendering is implemented.
- History DB growth will increase list load costs unless projection queries are adopted.
- Multi-key/multi-model rotation complexity can increase network attempts; add adaptive policy based on recent latency/error rates per model.
- Offline-first performance will benefit from introducing summary preview columns, compaction jobs, and periodic cache pruning strategy.

## Conclusion

YTSummary has a solid foundation, but runtime smoothness during streaming is currently the dominant performance risk. The most valuable path is to redesign streaming UI updates as incremental, cadence-controlled events and reduce unnecessary data movement across layers. In parallel, tightening Room queries and response resource handling will improve memory, throughput, and stability.

Recommended execution order:
1. Streaming delta model + UI cadence throttling.
2. Room projection query for history list.
3. Network response lifecycle hardening (use blocks everywhere).
4. Baseline profile generation and CI validation.
5. Startup deferral and frame-metric verification.
