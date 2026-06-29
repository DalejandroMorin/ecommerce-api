# Proposal: Hexagonal Domain Consistency

## Intent

Six gaps found during a hexagonal architecture audit: Carrito and Pedido domain models lack `validar()` methods (validation happens inline in use cases), UsuarioUseCase and AuthUseCase bypass domain validation or encode passwords before validating, a Spring Security adapter lives in the wrong layer, and AGENTS.md describes an outdated architecture. This change fixes all six to enforce hexagonal boundaries consistently.

## Scope

### In Scope
1. Add `validar()` to `domain/carrito/Carrito.java` — empty cart, invalid items
2. Add `validar()` to `domain/pedido/Pedido.java` — empty order, insufficient stock
3. Fix `application/usuario/UsuarioUseCase.java` — call `usuario.validar()` instead of inline duplication
4. Fix `application/auth/AuthUseCase.java.register()` — validate BEFORE encoding password
5. Move `application/auth/UserDetailsImpl.java` → `infrastructure/security/UserDetailsImpl.java`
6. Update `AGENTS.md` — all 4 modules now follow hexagonal pattern

### Out of Scope
- Adding new validation rules beyond what exists in the codebase
- Refactoring other modules to hexagonal (already complete per audit)
- Integration/Testcontainers tests for validation flows
- Creating new specs for domain-models or user-auth in `openspec/specs/`

## Capabilities

### New Capabilities
None

### Modified Capabilities
- `domain-models`: Carrito and Pedido gain `validar()` methods enforcing business rules inline. Existing `Producto` `validar()` unaffected.
- `user-auth`: `AuthUseCase.register()` encodes password AFTER domain validation. `UsuarioUseCase` calls `usuario.validar()` before persistence.

## Approach

TDD-first: write failing tests exposing each gap, then fix domain models, then fix use cases, then relocate `UserDetailsImpl`, then update docs. Order: validation fixes (domain → application), infrastructure move, documentation. Each issue is a standalone commit for clean rollback.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `domain/carrito/Carrito.java` | Modified | Add `validar()` method |
| `domain/pedido/Pedido.java` | Modified | Add `validar()` method |
| `application/usuario/UsuarioUseCase.java` | Modified | Replace inline validation with `validar()` call |
| `application/auth/AuthUseCase.java` | Modified | Move password encode after validation |
| `application/auth/UserDetailsImpl.java` | Removed | Relocated to infrastructure |
| `infrastructure/security/UserDetailsImpl.java` | New | New home for framework adapter |
| `AGENTS.md` | Modified | Update architecture description |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Auth flow breakage from reordered validation | Low | Existing auth tests validate flow; write new test for password-state-after-validation |
| Broken imports after UserDetailsImpl move | Low | Update all import references; compile-check before commit |
| AGENTS.md context in `openspec/config.yaml` also stale | Medium | Update config.yaml context in same docs commit |

## Rollback Plan

Each issue is an independent commit. Revert the offending commit if a specific fix breaks. The 6 commits can be reverted individually without affecting other work.

## Dependencies

None.

## Success Criteria

- [ ] `Carrito.validar()` exists and is called by its use case
- [ ] `Pedido.validar()` exists and is called by its use case
- [ ] `UsuarioUseCase` calls `usuario.validar()` — no inline validation logic
- [ ] `AuthUseCase.register()` encodes password AFTER `usuario.validar()` passes
- [ ] `UserDetailsImpl` lives in `infrastructure/security/` with no broken imports
- [ ] `AGENTS.md` and `openspec/config.yaml` describe current hexagonal state
- [ ] All existing tests pass
