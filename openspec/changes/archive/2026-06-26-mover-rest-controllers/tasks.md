# Tasks: mover-rest-controllers

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~350–700 per module; ~2000 total |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: Auth → PR 2: Usuario → PR 3: Carrito → PR 4: Pedido |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

```
Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High
```

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Migrar Auth | PR 1 | ~350 chg lines; base = main. Controller + 3 DTOs + test |
| 2 | Migrar Usuario | PR 2 | ~420 chg lines; base = main. Controller + 2 DTOs + test |
| 3 | Migrar Carrito | PR 3 | ~500 chg lines; base = main. Controller + 2 DTOs + test |
| 4 | Migrar Pedido | PR 4 | ~710 chg lines; base = main. Controller + 2 DTOs + test |

Each module is independent from the others — no ordering dependency between PRs. Review them in any sequence. Per-unit totals are slightly over 400 but trivially reviewable: mechanical package renames.

## Phase 1: Auth

- [x] 1.1 Create `infrastructure/rest/auth/AuthController.java` — move from `auth.controller` package
- [x] 1.2 Create `infrastructure/rest/auth/AuthResponseDTO.java`, `LoginRequestDTO.java`, `RegisterRequestDTO.java` — move from `auth.dto` package
- [x] 1.3 Move `AuthControllerTest.java` to `infrastructure/rest/auth/` — update package + imports
- [x] 1.4 Delete `auth/controller/` and `auth/dto/` directories

## Phase 2: Usuario

- [x] 2.1 Create `infrastructure/rest/usuario/UsuarioController.java` — move from `usuario.controller` package
- [x] 2.2 Create `infrastructure/rest/usuario/UsuarioRequestDTO.java`, `UsuarioResponseDTO.java` — move from `usuario.dto`
- [x] 2.3 Move `UsuarioControllerTest.java` to `infrastructure/rest/usuario/` — update package + imports
- [x] 2.4 Delete `usuario/controller/` and `usuario/dto/` directories

## Phase 3: Carrito

- [x] 3.1 Create `infrastructure/rest/carrito/CarritoController.java` — move from `carrito.controller` package
- [x] 3.2 Create `infrastructure/rest/carrito/CarritoResponseDTO.java`, `ItemCarritoDTO.java` — move from `carrito.dto`
- [x] 3.3 Move `CarritoControllerTest.java` to `infrastructure/rest/carrito/` — update package + imports
- [x] 3.4 Delete `carrito/controller/` and `carrito/dto/` directories

## Phase 4: Pedido

- [x] 4.1 Create `infrastructure/rest/pedido/PedidoController.java` — move from `pedido.controller` package
- [x] 4.2 Create `infrastructure/rest/pedido/PedidoResponseDTO.java`, `DetallePedidoDTO.java` — move from `pedido.dto`
- [x] 4.3 Move `PedidoControllerTest.java` to `infrastructure/rest/pedido/` — update package + imports
- [x] 4.4 Delete `pedido/controller/` and `pedido/dto/` directories

## Phase 5: Verify

- [x] 5.1 Run `./mvnw clean compile` — must compile without errors
- [x] 5.2 Run `./mvnw clean test` — all existing tests pass (0 new tests needed)
- [x] 5.3 Confirm zero files remain in `{auth,usuario,carrito,pedido}/controller/` or `dto/`
