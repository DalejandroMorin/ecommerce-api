# Tasks: Hexagonal Domain Consistency

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 150–250 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-always |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

## Phase 1: Domain Validation — RED (Tests)

- [x] 1.1 Write `CarritoTest` — `validar_Vacio_LanzaExcepcion`, `validar_ConItems_NoLanzaExcepcion`
- [x] 1.2 Write `PedidoTest` — `validar_Vacio_LanzaExcepcion`, `validar_CantidadInvalida_LanzaExcepcion`, `validar_Valido_NoLanzaExcepcion`

## Phase 2: Domain Validation — GREEN (Implementation)

- [x] 2.1 Add `validar()` to `domain/carrito/Carrito.java` — reject null/empty items
- [x] 2.2 Add `validar()` to `domain/pedido/Pedido.java` — reject null/empty detalles, non-positive cantidad per detalle
- [x] 2.3 `./mvnw test` — confirm new domain tests pass (REFACTOR check)

## Phase 3: Use Case Fixes — RED (Test Updates)

- [x] 3.1 Update `UsuarioServiceTest` — expect `IllegalArgumentException` from delegated `usuario.validar()` instead of inline exception
- [x] 3.2 Update `AuthUseCaseTest` — verify `passwordEncoder.encode()` is called AFTER `usuario.validar()` on raw password
- [x] 3.3 Update `PedidoServiceTest` — update exception expectations to match delegated `validar()`

## Phase 4: Use Case Fixes — GREEN (Implementation)

- [x] 4.1 Fix `application/usuario/UsuarioUseCase` — call `usuario.validar()` instead of inline email/password validation
- [x] 4.2 Fix `application/auth/AuthUseCase.register()` — reorder to: construct Usuario → `validar()` → encode password
- [x] 4.3 Wire `carrito.validar()` in `PedidoUseCase` and `pedido.validar()` after building
- [x] 4.4 `./mvnw test` — all tests pass with updated exception types (REFACTOR check)

## Phase 5: Infrastructure Relocation

- [x] 5.1 Create `infrastructure/security/UserDetailsImpl.java` — same content, updated package
- [x] 5.2 Delete `application/auth/UserDetailsImpl.java`
- [x] 5.3 Update imports in `UserDetailsServiceImpl`, `JwtAuthFilter`, `AuthUseCaseTest`
- [x] 5.4 `./mvnw test` — compilation and tests pass (REFACTOR check)

## Phase 6: Documentation

- [x] 6.1 Update `AGENTS.md` — remove "traditional layered" from auth, usuario, carrito, pedido; describe all 4 as hexagonal
- [x] 6.2 Update `openspec/config.yaml` — architecture context line to reflect all-modules hexagonal state
