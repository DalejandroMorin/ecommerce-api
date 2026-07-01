# Tasks: Consolidar Enums Duplicados

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 60-80 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Todos los cambios | PR 1 (single) | Refactor puro, solo imports + 3 archivos nuevos |

## Phase 1: Enum Source Files

- [x] 1.1 Crear `domain/common/Categoria.java` con los valores `ELECTRONICA, ROPA, HOGAR, JUGUETES, LIBROS, DEPORTES, OTROS`
- [x] 1.2 Crear `domain/common/Rol.java` con los valores `CLIENTE, ADMIN`
- [x] 1.3 Crear `domain/common/EstadoPedido.java` con los valores `PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO`

## Phase 2: Producto Module (Categoria)

- [x] 2.1 `Producto.java` — eliminar enum inline Categoria, importar `domain.common.Categoria`
- [x] 2.2 `ProductoEntity.java` — eliminar enum inline Categoria, importar `domain.common.Categoria`
- [x] 2.3 `ProductoMapper.java` — reemplazar `valueOf(name())` por asignación directa
- [x] 2.4 `JpaProductoRepositoryAdapter.java` — reemplazar `Categoria.valueOf()` por asignación directa
- [x] 2.5 `ProductoJpaRepository.java` — actualizar import Categoria
- [x] 2.6 `ProductoUseCase.java`, `ProductoController.java`, `ProductoRequestDTO.java`, `ProductoResponseDTO.java` — actualizar imports

## Phase 3: Usuario Module (Rol)

- [x] 3.1 `Usuario.java` — eliminar enum inline Rol, importar `domain.common.Rol`
- [x] 3.2 `UsuarioEntity.java` — eliminar enum inline Rol, importar `domain.common.Rol`
- [x] 3.3 `UsuarioMapper.java` — reemplazar `valueOf(name())` por asignación directa
- [x] 3.4 `UsuarioRequestDTO.java`, `UsuarioResponseDTO.java`, `AuthUseCase.java`, `UsuarioUseCase.java`, `JwtAuthFilter.java`, `UserDetailsImpl.java` — actualizar imports

## Phase 4: Pedido Module (EstadoPedido)

- [x] 4.1 `Pedido.java` — eliminar enum inline EstadoPedido, importar `domain.common.EstadoPedido`
- [x] 4.2 `PedidoEntity.java` — eliminar enum inline EstadoPedido, importar `domain.common.EstadoPedido`
- [x] 4.3 `PedidoMapper.java` — reemplazar `valueOf(name())` por asignación directa
- [x] 4.4 `JpaPedidoRepositoryAdapter.java` — reemplazar `valueOf(name())` por asignación directa
- [x] 4.5 `PedidoUseCase.java`, `PagoSimuladoUseCase.java`, `PedidoController.java`, `PedidoResponseDTO.java` — actualizar imports

## Phase 5: Tests + Verify

- [x] 5.1 Actualizar imports de enums en ~14 archivos de test
- [x] 5.2 `mvnw clean compile` — BUILD SUCCESS
- [x] 5.3 `mvnw test` — 70 tests PASS (0 failures). 55 errors son pre-existentes: ApplicationContext en @DataJpaTest (requieren PostgreSQL)
