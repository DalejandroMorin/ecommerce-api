# Design: Migración Hexagonal Completa

## Technical Approach

Refactor estructural puro: mover controllers + DTOs de 5 módulos a `infrastructure/rest/{modulo}/`, migrar `ProductoEntity`/`ProductoJpaRepository` de old packages a `infrastructure/persistence/jpa/`, y actualizar imports en use cases que referencian DTOs. Cero cambios de contrato API o lógica de negocio.

```
Layered (old)                        →  Hexagonal (new)
─────────────────────────────────────────────────────────
producto/controller/ProductoController → infrastructure/rest/producto/ProductoController
producto/dto/*                          → infrastructure/rest/producto/*DTO
producto/model/ProductoEntity           → infrastructure/persistence/jpa/entity/ProductoEntity
producto/repository/ProductoJpaRepository → infrastructure/persistence/jpa/repository/
usuario/controller/*                    → infrastructure/rest/usuario/*
usuario/dto/*                           → infrastructure/rest/usuario/*
carrito/controller/*                    → infrastructure/rest/carrito/*
carrito/dto/*                           → infrastructure/rest/carrito/*
pedido/controller/*                     → infrastructure/rest/pedido/*
pedido/dto/*                            → infrastructure/rest/pedido/*
auth/controller/*                       → infrastructure/rest/auth/*
auth/dto/*                              → infrastructure/rest/auth/*
```

## Architecture Decisions

### Decision: Rest adapter location
| Option | Tradeoff |
|--------|----------|
| `infrastructure/rest/{modulo}/` (chosen) | Controllers + DTOs juntos, un solo paquete por módulo |
| `infrastructure/rest/{modulo}/controller/` + `dto/` | Más profundo, sin beneficio real para ~2 archivos |
| `infrastructure/rest/{modulo}/controller/` imports from `../dto/` | Paquetes separados, más imports |

**Rationale**: Seguir patrón implícito de `producto/controller/` + `producto/dto/` (mismo nivel). Un solo paquete reduce navegación.

### Decision: ProductoEntity migration
| Option | Tradeoff |
|--------|----------|
| Mover a `infrastructure/persistence/jpa/entity/` (chosen) | Consistente con UsuarioEntity, CarritoEntity, etc. |
| Dejar en `producto/model/` | Perpetúa deuda técnica; módulo nunca estaría limpio |

**Rationale**: Los otros 4 módulos ya tienen su Entity en infra; Producto era el único fuera de lugar.

### Decision: AuthUseCase — agregar validación de dominio
**Choice**: Llamar `usuario.validar()` en `register()` y usar constructor parametrizado en vez de setters manuales.
**Rationale**: Alinea con patrón de `ProductoUseCase.crear()`. `Usuario.validar()` ya existe y lanza `IllegalArgumentException` (manejado → 400). Sin cambio de contrato API.
**Not in scope**: Agregar `@Valid` a auth DTOs (sería cambio de comportamiento).

### Decision: Tests — delete + recreate
**Choice**: Eliminar tests legacy de controller (paquete old) y crear nuevos en `infrastructure/rest/{modulo}/`. Tests de use case (service) se quedan.
**Rationale**: Los tests de controller instancian el controller por clase — no pueden coexistir clases duplicadas en classpath. Use case tests testean `application/`, no cambian.

## Data Flow

```
HTTP Request
  → infrastructure/rest/{modulo}/{ModuloController}
    → application/{modulo}/{ModuloUseCase} (via domain repository port)
      → domain/{modulo}/{Modelo} (pure Java, validar())
      → infrastructure/persistence/jpa/adapter/*Adapter (mapper to entity)
        → infrastructure/persistence/jpa/entity/*Entity (JPA)
          → DB
```

Sin cambios en el flujo actual — solo cambian los packages del adapter REST.

## File Changes

### CREATE (17 main + 6 test = 23 files)

| File | Description |
|------|-------------|
| `infrastructure/rest/producto/ProductoController.java` | Controller hex, desde old |
| `infrastructure/rest/producto/ProductoRequestDTO.java` | DTO hex, desde old |
| `infrastructure/rest/producto/ProductoResponseDTO.java` | DTO hex, desde old (sin ctor desde ProductoEntity) |
| `infrastructure/rest/usuario/UsuarioController.java` | Desde `usuario/controller/` |
| `infrastructure/rest/usuario/UsuarioRequestDTO.java` | Desde `usuario/dto/` |
| `infrastructure/rest/usuario/UsuarioResponseDTO.java` | Desde `usuario/dto/` |
| `infrastructure/rest/carrito/CarritoController.java` | Desde `carrito/controller/` |
| `infrastructure/rest/carrito/CarritoResponseDTO.java` | Desde `carrito/dto/` |
| `infrastructure/rest/carrito/ItemCarritoDTO.java` | Desde `carrito/dto/` |
| `infrastructure/rest/pedido/PedidoController.java` | Desde `pedido/controller/` |
| `infrastructure/rest/pedido/PedidoResponseDTO.java` | Desde `pedido/dto/` |
| `infrastructure/rest/pedido/DetallePedidoDTO.java` | Desde `pedido/dto/` |
| `infrastructure/rest/auth/AuthController.java` | Desde `auth/controller/` |
| `infrastructure/rest/auth/AuthResponseDTO.java` | Desde `auth/dto/` |
| `infrastructure/rest/auth/LoginRequestDTO.java` | Desde `auth/dto/` |
| `infrastructure/rest/auth/RegisterRequestDTO.java` | Desde `auth/dto/` |
| `infrastructure/persistence/jpa/entity/ProductoEntity.java` | Desde `producto/model/` |
| `infrastructure/persistence/jpa/repository/ProductoJpaRepository.java` | Desde `producto/repository/` |
| `src/test/java/.../infrastructure/rest/producto/ProductoControllerTest.java` | Desde old test |
| `src/test/java/.../infrastructure/rest/auth/AuthControllerTest.java` | Desde old test |
| `src/test/java/.../infrastructure/rest/carrito/CarritoControllerTest.java` | Desde old test |
| `src/test/java/.../infrastructure/rest/usuario/UsuarioControllerTest.java` | Desde old test |
| `src/test/java/.../infrastructure/rest/pedido/PedidoControllerTest.java` | Desde old test |

### DELETE (~20 files)
Todas las clases bajo `src/main/java/.../producto/controller/`, `producto/dto/`, `producto/model/`, `producto/repository/`, `usuario/controller/`, `usuario/dto/`, `carrito/controller/`, `carrito/dto/`, `pedido/controller/`, `pedido/dto/`, `auth/controller/`, `auth/dto/` + sus tests correspondientes en `src/test/java/...`.

### MODIFY (7 files)

| File | Change |
|------|--------|
| `application/producto/ProductoUseCase.java` | Import `producto.dto.*` → `infrastructure.rest.producto.*` |
| `application/usuario/UsuarioUseCase.java` | Import `usuario.dto.*` → `infrastructure.rest.usuario.*` |
| `application/carrito/CarritoUseCase.java` | Import `carrito.dto.*` → `infrastructure.rest.carrito.*` |
| `application/pedido/PedidoUseCase.java` | Import `pedido.dto.*` → `infrastructure.rest.pedido.*` |
| `application/auth/AuthUseCase.java` | Import `auth.dto.*` → `infrastructure.rest.auth.*` + agregar `usuario.validar()` + usar ctor parametrizado |
| `infrastructure/persistence/jpa/mapper/ProductoMapper.java` | Import `producto.model.ProductoEntity` → `...jpa.entity.ProductoEntity` |
| `infrastructure/persistence/jpa/adapter/JpaProductoRepositoryAdapter.java` | Import `producto.model.*` + `producto.repository.*` → nuevas ubicaciones |

## Testing Strategy

| Layer | Qué Testear | Enfoque |
|-------|-------------|---------|
| Controller REST | Cada endpoint: status code, response body, validación, errores | MockMvc standalone + mock UseCase |
| Use Case | (sin cambios — tests existentes se quedan) | `@ExtendWith(MockitoExtension.class)` |
| Producto Adapter/Mapper | (sin cambios en lógica, solo imports) | Tests existentes pasan si imports correctos |

Service tests (`usuario/service/`, `pedido/service/`, `carrito/service/`, `auth/service/`) no se mueven — testean `application/` que no cambia de paquete.

## Open Questions

- [ ] **Delivery order**: PR único ~500+ líneas o PRs encadenados por módulo (producto → usuario → carrito → pedido → auth)? El tamaño sugiere PRs encadenados.
- [ ] **Packages vacíos**: Después de eliminar old packages, ¿eliminar directorios vacíos manualmente o `git clean -fd`?
