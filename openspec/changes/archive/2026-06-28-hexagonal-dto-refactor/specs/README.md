# Delta Spec — hexagonal-dto-refactor

**No behavioral changes — pure structural refactoring. All existing requirements remain unchanged.**

## Summary

This change relocates 11 DTOs from `infrastructure/rest/{modulo}/` to `application/{modulo}/dto/` and `UserDetailsImpl` from `infrastructure/security/` to `application/auth/`. It enforces the hexagonal architecture dependency rule without altering any system behavior, API contract, or requirement.

## Requirements

| Section | Count | Detail |
|---------|-------|--------|
| ADDED | 0 | No new capabilities introduced |
| MODIFIED | 0 | No existing capability changed |
| REMOVED | 0 | No capability removed |
| RENAMED | 0 | No capability renamed |

## Verification

- ✅ `./mvnw clean compile` passes (commit `4967102`)
- ✅ `./mvnw test` passes — all 10 test suites green
- ✅ Import paths updated across 18 affected files
- ✅ No old `infrastructure/rest/*/dto/` references remain
