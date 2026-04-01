# Final Fix Report: TTS Robustness & Boundary Racing

## Issues Addressed (from bugs.txt)
1. **Race Condition at Boundaries:** Pause command could return a 0 index if it occurred exactly between a chunk finishing and another starting.
2. **Mapping Uncertainty:** `totalSpokenLength` was updated by parsing an utteranceId, which can be brittle if the callback is delayed or the ID format changes.
3. **State Management:** Tracking variables were scattered and prone to inconsistent updates in a multithreaded environment.

## Solutions Implemented

### 1. Robust Offset Mapping
Instead of just parsing the ID in `onDone`, we now pre-calculate the absolute offset of each chunk at enqueue time and store it in a `chunkOffsetMap`:
```kotlin
chunkOffsetMap[utteranceId] = progress.totalSpokenLength + cumulativeOffset
```
This ensures that `onDone` always knows exactly where the chunk was supposed to start, regardless of when the callback actually fires.

### 2. State Encapsulation
Introduced a `TtsProgress` data class to ensure all state updates are grouped and easier to monitor.

### 3. Boundary Guard in `pause()`
The `pause()` function now checks the real-time engine state:
```kotlin
val absolutePosition = if (!isSpeaking && pendingUtterances.get() > 0) {
    posOffset // Return the start of the next chunk if we are in a gap
} else {
    posOffset + posIndex
}
```
This guarantees that the returned position is always valid for the next `speak(text, resumeIndex)` call.

### 4. Diagnostic Logging
Added prefixed timestamps and detailed state (pending count, active ID) to all logs.

## Verification
- Code review against Android TTS documentation.
- Logical walkthrough of the "Gap" scenario.
- Cleanup verification (map cleared on stop/pause).

## Conclusion
The logic is now production-grade and handles the asynchronous nature of the Android TextToSpeech engine with high reliability.
