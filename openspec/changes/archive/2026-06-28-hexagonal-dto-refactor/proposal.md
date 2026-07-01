# Proposal: hexagonal-dto-refactor

## Intent

Enforce the hexagonal architecture dependency rule: the application layer must NOT import from infrastructure. All DTOs and `UserDetailsImpl` lived in `infrastructure/` packages, creating a circular dependency risk and violating the direction of dependencies (inward → domain, outward → infrastructure).

## Scope

### In Scope
- Move 11 DTOs from `infrastructure/rest/{modulo}/` to `application/{modulo}/dto/`
- Move `UserDetailsImpl` from `infrastructure/security/` to `application/auth/`
- Update 5 use cases, 5 controllers, 2 security files, and 5 test files to reference new packages
- Remove dead entity constructors from `CarritoResponseDTO` and `ItemCarritoDTO`

### Out of Scope
- No behavioral or API surface changes (pure structural refactor)
- No spec-level requirement changes
- No new capabilities introduced

## Capabilities

### New Capabilities
None — pure structural refactoring, no new functional behavior.

### Modified Capabilities
None — no existing capability changed its requirements.

## Approach

1. Relocate each DTO preserving its class name while moving to `application/{modulo}/dto/`
2. Relocate `UserDetailsImpl` to `application/auth/`
3. Rewrite imports across 18 affected files to reference the new package paths
4. Simplify two DTOs by removing entity constructor overloads that were dead code after the separation
5. Verify compilation with `./mvnw clean compile` and test pass with `./mvnw test`

All work was implemented, committed (4967102), and pushed to `main` in a single pass.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `application/{auth,carrito,pedido,producto,usuario}/dto/` | New | 11 DTOs relocated here |
| `application/auth/UserDetailsImpl.java` | Modified | Relocated from `infrastructure/security/` |
| `application/*/UseCase.java` (5 files) | Modified | Updated imports |
| `infrastructure/rest/*Controller.java` (5 files) | Modified | Updated imports |
| `infrastructure/security/*Filter.java` (2 files) | Modified | Updated imports |
| Test files (5 files) | Modified | Updated imports |
| DTO entity constructors | Removed | Dead code from 2 DTOs |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Import resolution errors | Low | Verified via `./mvnw clean compile` |
| Missed import updates | Low | Grep for old package references across all source files |

## Rollback Plan

Revert commit 4967102 via `git revert` and verify with `./mvnw clean compile test`.

## Dependencies

None — self-contained structural refactor.

## Success Criteria

- [x] `./mvnw clean compile` passes (02:48 min, BUILD SUCCESS)
- [x] `./mvnw test` passes (all 10 test suites green)
- [x] No compilation warnings for deprecated or dead imports
