# Phase 02: API Client Refactoring
Status: ✅ Completed
Dependencies: Phase 01

## Objective
Apply the fixes to `GeminiApiClient.kt` to resolve the 3 identified issues.

## Requirements
### Functional
- [x] Implement `QuotaExceededException`, `ServerBusyException`, and `ModelUnavailableException`.
- [x] Modify `summarize` to catch specific exceptions and control rotation flow (continue vs break).
- [x] Implement error body parsing for 429 to distinguish RPM from RPD.
- [x] Update `shouldRetry` condition in `retryWithBackoff` to check `!hasStarted`.
- [x] Prevent automatic model/key jumping if `hasStarted` is true to avoid UI duplication.

## Implementation Steps
1. [x] Update imports in `GeminiApiClient.kt`.
2. [x] Define custom Exception classes at the bottom of the file.
3. [x] Refactor the `summarize` flow to include nested `try-catch` and `when` logic for HTTP codes.
4. [x] Ensure `errorBody` is read and closed properly.
5. [x] Integrate `quotaManager.markCooldown` for RPM/503 errors.

## Files to Create/Modify
- `app/src/main/java/com/skul9x/ytsummary/api/GeminiApiClient.kt` - Main refactoring.
- `app/src/test/java/com/skul9x/ytsummary/api/GeminiApiClientRefinedTest.kt` - Added new tests.
- `app/src/test/java/com/skul9x/ytsummary/api/GeminiApiClientDiagnosticTest.kt` - Updated diagnostic tests.

## Test Criteria
- [x] Code compiles without errors.
- [x] Unit tests (if any) for `GeminiApiClient` pass.

---
Next Phase: [Phase 03: Validation & Integration Testing](phase-03-validation.md)

