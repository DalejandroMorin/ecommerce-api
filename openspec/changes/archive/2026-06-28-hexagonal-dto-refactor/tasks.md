# Tasks: Hexagonal DTO Refactor

**Retroactive** — all work completed, committed, and pushed at `4967102`.

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~224 (37 files, +170 -54) |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

## Phase 1: Auth DTOs + UserDetailsImpl

- [x] 1.1 Create `application/auth/dto/AuthResponseDTO.java` (moved from `infrastructure/rest/auth/`)
- [x] 1.2 Create `application/auth/dto/LoginRequestDTO.java` (moved from `infrastructure/rest/auth/`)
- [x] 1.3 Create `application/auth/dto/RegisterRequestDTO.java` (moved from `infrastructure/rest/auth/`)
- [x] 1.4 Create `application/auth/UserDetailsImpl.java` (moved from `infrastructure/security/`)

## Phase 2: Carrito DTOs (with simplification)

- [x] 2.1 Create `application/carrito/dto/CarritoResponseDTO.java` — moved from `infrastructure/rest/carrito/`; removed dead entity constructor
- [x] 2.2 Create `application/carrito/dto/ItemCarritoDTO.java` — moved from `infrastructure/rest/carrito/`; removed dead entity constructor

## Phase 3: Pedido, Producto, Usuario DTOs

- [x] 3.1 Create `application/pedido/dto/DetallePedidoDTO.java` (moved from `infrastructure/rest/pedido/`)
- [x] 3.2 Create `application/pedido/dto/PedidoResponseDTO.java` (moved from `infrastructure/rest/pedido/`)
- [x] 3.3 Create `application/producto/dto/ProductoRequestDTO.java` (moved from `infrastructure/rest/producto/`)
- [x] 3.4 Create `application/producto/dto/ProductoResponseDTO.java` (moved from `infrastructure/rest/producto/`)
- [x] 3.5 Create `application/usuario/dto/UsuarioRequestDTO.java` (moved from `infrastructure/rest/usuario/`)
- [x] 3.6 Create `application/usuario/dto/UsuarioResponseDTO.java` (moved from `infrastructure/rest/usuario/`)

## Phase 4: Import Updates — Use Cases

- [x] 4.1 Update `AuthUseCase.java` — import DTOs from `application.auth.dto`
- [x] 4.2 Update `CarritoUseCase.java` — import DTOs from `application.carrito.dto`
- [x] 4.3 Update `PedidoUseCase.java` — import DTOs from `application.pedido.dto`
- [x] 4.4 Update `ProductoUseCase.java` — import DTOs from `application.producto.dto`
- [x] 4.5 Update `UsuarioUseCase.java` — import DTOs from `application.usuario.dto`

## Phase 5: Import Updates — Controllers & Security

- [x] 5.1 Update `AuthController.java` — DTO imports to `application.auth.dto`
- [x] 5.2 Update `CarritoController.java` — DTO imports to `application.carrito.dto`
- [x] 5.3 Update `PedidoController.java` — DTO imports to `application.pedido.dto`
- [x] 5.4 Update `ProductoController.java` — DTO imports to `application.producto.dto`
- [x] 5.5 Update `UsuarioController.java` — DTO imports to `application.usuario.dto`
- [x] 5.6 Update `JwtAuthFilter.java` — `UserDetailsImpl` import to `application.auth`
- [x] 5.7 Update `UserDetailsServiceImpl.java` — `UserDetailsImpl` import to `application.auth`

## Phase 6: Test Import Updates & Verification

- [x] 6.1 Update 5 test files — imports to new DTO / UserDetailsImpl locations
- [x] 6.2 Remove old DTO files from `infrastructure/rest/{auth,carrito,pedido,producto,usuario}/`
- [x] 6.3 Remove old `UserDetailsImpl.java` from `infrastructure/security/`
- [x] 6.4 Verify: `./mvnw clean compile test` — all 117 tests pass
