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
Chain strategy: stacked-to-main (single PR — merged)
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | PR | Notes |
|------|------|----|-------|
| 1 | Migración completa (todos los módulos) | PR #1 mergeado | Commit único con domain/application/infrastructure para todos los módulos |

### Nota sobre el alcance real

El commit `9a713c8` incluye domain/application/infrastructure/persistence/security para TODOS los módulos.
Los controllers de usuario/carrito/pedido/auth quedaron en sus paquetes actuales (modificados con imports hexagonales)
pero NO fueron movidos a `infrastructure/rest/`. Eso queda como mejora futura si se desea.

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

## Phase 2: Usuario Migration (archivos de capa hexagonal completados)

- [x] 2.4 Modificar imports en `application/usuario/UsuarioUseCase.java`
- [x] 2.6 Crear `UsuarioControllerTest` (MockMvc standalone)
- [~] 2.1-2.3, 2.5 Controller/DTOs legacy — NO movidos a infrastructure/rest/ (funcionan en ubicación actual)

## Phase 3: Carrito Migration (archivos de capa hexagonal completados)

- [x] 3.4 Modificar imports en `application/carrito/CarritoUseCase.java`
- [x] 3.6 Crear `CarritoControllerTest` (MockMvc standalone)
- [~] 3.1-3.3, 3.5 Controller/DTOs legacy — NO movidos a infrastructure/rest/ (funcionan en ubicación actual)

## Phase 4: Pedido Migration (archivos de capa hexagonal completados)

- [x] 4.4 Modificar imports en `application/pedido/PedidoUseCase.java`
- [x] 4.6 Crear `PedidoControllerTest` (MockMvc standalone)
- [~] 4.1-4.3, 4.5 Controller/DTOs legacy — NO movidos a infrastructure/rest/ (funcionan en ubicación actual)

## Phase 5: Auth Migration + Refactor

- [x] 5.5 Modificar `AuthUseCase`: imports + `usuario.validar()` + ctor parametrizado
- [x] 5.7 Crear `AuthControllerTest` (MockMvc standalone)
- [~] 5.1-5.4, 5.6 Controller/DTOs legacy — NO movidos a infrastructure/rest/ (funcionan en ubicación actual)

## Phase 6: Final Verification

- [~] 6.1 Limpiar directorios vacíos — parcial
- [x] 6.2 Ejecutar `./mvnw clean test` — 54/54 tests OK
- [x] 6.3 Verificar cero referencias a packages old — confirmado

> **Leyenda**: [x] completado | [~] diferido/parcial — el core de la migración hexagonal (domain/application/infrastructure) está completo para todos los módulos. Los controllers legacy quedan en su lugar actual como mejora futura.
