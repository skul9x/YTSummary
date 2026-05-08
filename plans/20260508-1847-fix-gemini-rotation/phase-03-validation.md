# Phase 03: Validation & Integration Testing
Status: ✅ Completed
Dependencies: Phase 02

## Objective
Verify that the fixes work as expected in real-world scenarios or simulated error conditions.

## Requirements
### Functional
- [x] Verify 429 RPM error triggers a 5-minute cooldown.
- [x] Verify 429 RPD error triggers a 30-hour ban.
- [x] Verify that a disconnected stream (after starting) emits an Error and stops, instead of retrying or switching keys.
- [x] Verify that 400/404 errors skip the current model entirely.

## Implementation Steps
1. [x] (Manual/Unit Test) Simulate HTTP 429 with "Rate limit" in body and check `ModelQuotaManager` state.
2. [x] (Manual/Unit Test) Simulate HTTP 429 with "Quota" in body and check `ModelQuotaManager` state.
3. [x] (Manual/Unit Test) Simulate `IOException` during stream and check if UI receives duplicate text.
4. [x] (Manual/Unit Test) Simulate HTTP 400 and verify rotation moves to the next model.

## Files to Create/Modify
- `reports/validation_report.md` - Document results.

## Test Criteria
- [x] All 3 identified issues are confirmed fixed.
- [x] Application remains stable during API rotation.

---
Next Phase: Complete
