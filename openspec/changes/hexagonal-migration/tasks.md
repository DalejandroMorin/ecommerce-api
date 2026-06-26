# Tasks: Migración Hexagonal Completa

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1700 (±200) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: Producto → PR 2: Usuario → PR 3: Carrito → PR 4: Pedido → PR 5: Auth |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Producto: entity/repo move + controller + tests | PR 1 | Incluye entity, JpaRepository, imports |
| 2 | Usuario: controller + DTOs + tests | PR 2 | Independiente, solo REST layer |
| 3 | Carrito: controller + DTOs + tests | PR 3 | Independiente |
| 4 | Pedido: controller + DTOs + tests | PR 4 | Independiente |
| 5 | Auth: controller + DTOs + AuthUseCase + tests | PR 5 | Refactor validación en use case |

## Phase 1: Producto Cleanup + Migration

- [x] 1.1 Crear `infrastructure/rest/producto/ProductoController.java`
- [x] 1.2 Crear `infrastructure/rest/producto/ProductoRequestDTO.java`
- [x] 1.3 Crear `infrastructure/rest/producto/ProductoResponseDTO.java`
- [x] 1.4 Mover `ProductoEntity` a `infrastructure/persistence/jpa/entity/`
- [x] 1.5 Mover `ProductoJpaRepository` a `infrastructure/persistence/jpa/repository/`
- [x] 1.6 Modificar imports en `ProductoMapper` y `JpaProductoRepositoryAdapter`
- [x] 1.7 Modificar imports en `application/producto/ProductoUseCase.java`
- [x] 1.8 Eliminar old `producto/controller/`, `dto/`, `model/`, `repository/`
- [x] 1.9 Crear `ProductoControllerTest` (MockMvc standalone)

## Phase 2: Usuario Migration

- [ ] 2.1 Crear `infrastructure/rest/usuario/UsuarioController.java`
- [ ] 2.2 Crear `infrastructure/rest/usuario/UsuarioRequestDTO.java`
- [ ] 2.3 Crear `infrastructure/rest/usuario/UsuarioResponseDTO.java`
- [ ] 2.4 Modificar imports en `application/usuario/UsuarioUseCase.java`
- [ ] 2.5 Eliminar old `usuario/controller/`, `dto/`
- [ ] 2.6 Crear `UsuarioControllerTest` (MockMvc standalone)

## Phase 3: Carrito Migration

- [ ] 3.1 Crear `infrastructure/rest/carrito/CarritoController.java`
- [ ] 3.2 Crear `infrastructure/rest/carrito/CarritoResponseDTO.java`
- [ ] 3.3 Crear `infrastructure/rest/carrito/ItemCarritoDTO.java`
- [ ] 3.4 Modificar imports en `application/carrito/CarritoUseCase.java`
- [ ] 3.5 Eliminar old `carrito/controller/`, `dto/`
- [ ] 3.6 Crear `CarritoControllerTest` (MockMvc standalone)

## Phase 4: Pedido Migration

- [ ] 4.1 Crear `infrastructure/rest/pedido/PedidoController.java`
- [ ] 4.2 Crear `infrastructure/rest/pedido/PedidoResponseDTO.java`
- [ ] 4.3 Crear `infrastructure/rest/pedido/DetallePedidoDTO.java`
- [ ] 4.4 Modificar imports en `application/pedido/PedidoUseCase.java`
- [ ] 4.5 Eliminar old `pedido/controller/`, `dto/`
- [ ] 4.6 Crear `PedidoControllerTest` (MockMvc standalone)

## Phase 5: Auth Migration + Refactor

- [ ] 5.1 Crear `infrastructure/rest/auth/AuthController.java`
- [ ] 5.2 Crear `infrastructure/rest/auth/AuthResponseDTO.java`
- [ ] 5.3 Crear `infrastructure/rest/auth/LoginRequestDTO.java`
- [ ] 5.4 Crear `infrastructure/rest/auth/RegisterRequestDTO.java`
- [ ] 5.5 Modificar `AuthUseCase`: imports + `usuario.validar()` + ctor parametrizado
- [ ] 5.6 Eliminar old `auth/controller/`, `dto/`
- [ ] 5.7 Crear `AuthControllerTest` (MockMvc standalone)

## Phase 6: Final Verification

- [ ] 6.1 Limpiar directorios vacíos (`git clean -fd`)
- [ ] 6.2 Ejecutar `./mvnw clean test` — 100% verde
- [ ] 6.3 Verificar cero referencias a packages old
