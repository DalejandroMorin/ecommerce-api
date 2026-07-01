# Design: Consolidar Enums Duplicados

## Technical Approach

Eliminar las 6 definiciones inline de `Categoria`, `Rol`, `EstadoPedido` (3 en domain, 3 en entities) moviéndolas a archivos únicos en `domain/common/`. Esto elimina la necesidad de las traducciones `valueOf(name())` en mappers y adapters, ya que ambos lados compartirán la misma clase de enum.

## Architecture Decisions

### Decision 1: Enums en `domain/common/` vs `infrastructure/persistence/jpa/enum/`

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| `domain/common/` | Los enums son conceptos de dominio compartidos entre capas; no se acoplan a JPA | ✅ Elegido |
| `infrastructure/.../jpa/enum/` | Acopla conceptos de dominio a la implementación de persistencia | ❌ Rechazado |

### Decision 2: Archivos separados vs `DomainEnums.java` único

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Archivos separados | Cada enum es independiente; navegación directa; convención Java estándar | ✅ Elegido |
| `DomainEnums.java` | Archivo único centralizado pero viola 1 clase por archivo y mezcla conceptos | ❌ Rechazado |

## Data Flow

Los mappers y adaptadores que hoy hacen `valueOf(name())` pasarán a asignación directa porque el mismo enum se usa en domain y entity:

```
Antes:  ProductoMapper.toEntity()
          domain.Categoria → valueOf(name()) → ProductoEntity.Categoria
Después: ProductoMapper.toEntity()
          Categoria → direct assignment → Categoria (misma clase)
```

## File Changes

### New Files (3)

| File | Description |
|------|-------------|
| `domain/common/Categoria.java` | Enum: `ELECTRONICA, ROPA, HOGAR, JUGUETES, LIBROS, DEPORTES, OTROS` |
| `domain/common/Rol.java` | Enum: `CLIENTE, ADMIN` |
| `domain/common/EstadoPedido.java` | Enum: `PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO` |

### Modified Files — Remove Inline Enums + Update Imports (17)

| File | Changes |
|------|---------|
| `domain/producto/Producto.java` | Remove inline `Categoria` enum, add `import domain.common.Categoria` |
| `domain/usuario/Usuario.java` | Remove inline `Rol` enum, add `import domain.common.Rol` |
| `domain/pedido/Pedido.java` | Remove inline `EstadoPedido` enum, add `import domain.common.EstadoPedido` |
| `entity/ProductoEntity.java` | Remove inline `Categoria` enum, add `import domain.common.Categoria` |
| `entity/UsuarioEntity.java` | Remove inline `Rol` enum, add `import domain.common.Rol` |
| `entity/PedidoEntity.java` | Remove inline `EstadoPedido` enum, add `import domain.common.EstadoPedido` |
| `mapper/ProductoMapper.java` | Replace `ProductoEntity.Categoria.valueOf(...)` / `Producto.Categoria.valueOf(...)` with direct assignment (same enum now) |
| `mapper/UsuarioMapper.java` | Same pattern — remove `valueOf(name())` |
| `mapper/PedidoMapper.java` | Same pattern — remove `valueOf(name())` |
| `adapter/JpaProductoRepositoryAdapter.java` | Remove `valueOf(name())` en `buscarConFiltros`; replace `ProductoEntity.Categoria` → `Categoria` |
| `adapter/JpaPedidoRepositoryAdapter.java` | Remove `PedidoEntity.EstadoPedido.valueOf(name())` |
| `ProductoUseCase.java` | `Producto.Categoria` → `Categoria` |
| `ProductoController.java` | `Producto.Categoria` → `Categoria` |
| `ProductoRepository.java` | `Producto.Categoria` → `Categoria` |
| `ProductoJpaRepository.java` | `ProductoEntity.Categoria` → `Categoria` |
| `PedidoController.java` | `Pedido.EstadoPedido` → `EstadoPedido` |
| `ProductoRequestDTO.java`, `ProductoResponseDTO.java`, `UsuarioRequestDTO.java`, `UsuarioResponseDTO.java`, `PedidoResponseDTO.java`, `PedidoUseCase.java`, `PagoSimuladoUseCase.java`, `AuthUseCase.java`, `UsuarioUseCase.java`, `JwtAuthFilter.java`, `UserDetailsImpl.java` | Replace `Xxx.Yyy` references with direct `import domain.common.Yyy` (~11 files, minor import changes) |

### Modified Files — Tests (10)

All references to `Producto.Categoria`, `Pedido.EstadoPedido`, `Usuario.Rol`, `ProductoEntity.Categoria`, `UsuarioEntity.Rol`, `PedidoEntity.EstadoPedido` in test files change to `com.david.ecommerce.domain.common.*`.

### Mapper Simplification Pattern

```java
// Antes
entity.setCategoria(ProductoEntity.Categoria.valueOf(domain.getCategoria().name()));

// Después
entity.setCategoria(domain.getCategoria());  // misma clase Categoria
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Compilación | Imports correctos en todos los archivos | `mvnw clean compile` |
| Unit tests | Comportamiento no alterado | `mvnw test` |
| Serialización | JSON enum names no cambian | Jackson usa `name()` por defecto — sin cambios |

## Migration / Rollout

No migration required. Rollback = `git revert`.

## Open Questions

None.
