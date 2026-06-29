# Design: Hexagonal DTO Refactor

## Technical Approach

Move all 11 DTOs from `infrastructure/rest/{modulo}/` to `application/{modulo}/dto/` and relocate `UserDetailsImpl` from `infrastructure/security/` to `application/auth/`. This is a pure structural refactor with zero behavioral change — no API contract, response format, or domain logic is altered.

The approach follows a simple relocation pattern: preserve class names and internal structure, update only the package declaration and all cross-file imports. Two DTOs (`CarritoResponseDTO`, `ItemCarritoDTO`) are simplified by removing dead entity constructor overloads that existed only because the DTOs lived at the infrastructure level.

## Architecture Decisions

### Decision: DTOs belong in the application layer

**Choice**: Relocated all DTOs to `application/{modulo}/dto/`

**Alternatives considered**:
| Option | Tradeoff | Decision |
|--------|----------|----------|
| Keep DTOs in `infrastructure/rest/{modulo}/` | ✓ Less movement — ✗ Violates hexagonal dependency rule; application imports infrastructure | Rejected |
| Keep DTOs in `domain/{modulo}/` | ✓ Central — ✗ Domain layer should not depend on frameworks (Lombok, Jakarta validation annotations on DTOs) | Rejected |
| Move DTOs to `application/{modulo}/dto/` | ✓ Enforces dependency rule; DTOs stay close to use cases that create them — Requires import updates across 18 files | Chosen |

**Rationale**: The hexagonal architecture dependency rule states that the application layer may import from domain, but NOT from infrastructure. DTOs are presentation concerns that map domain objects to wire format — they belong in the application layer alongside the use cases that produce them. Keeping them in infrastructure meant every `UseCase` imported from an outer layer, creating an inward dependency violation.

### Decision: UserDetailsImpl is an application concern

**Choice**: Moved `UserDetailsImpl` to `application/auth/`

**Alternatives considered**: Keep in `infrastructure/security/` — would force `AuthUseCase` to import from infrastructure when casting `Authentication.getPrincipal()`.

**Rationale**: `UserDetailsImpl` implements Spring Security's `UserDetails` interface. It's a framework adapter at its surface, but its logical role is bridging authenticated users into the application layer. The use case casts to it during login (`(UserDetailsImpl) authentication.getPrincipal()`). Moving it to `application/auth/` eliminates the infrastructure dependency from the application layer while keeping security infrastructure (`UserDetailsServiceImpl`, `JwtAuthFilter`) in `infrastructure/security/`.

## Data Flow

```
Controller (infrastructure/rest)
    ↓ @Valid @RequestBody with application/*/dto/*DTO
UseCase (application/*)
    ↑ Returns application/*/dto/*ResponseDTO
    ↓ Calls domain repository ports
Domain Model + Repository Port (domain/*)
    ↓ Implemented by
JPA Adapter (infrastructure/persistence)
```

The DTOs flow from outer (controller) through application back to outer. No infrastructure type crosses into the application layer.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `application/auth/UserDetailsImpl.java` | Move | From `infrastructure/security/` |
| `application/auth/dto/AuthResponseDTO.java` | Move | From `infrastructure/rest/auth/` |
| `application/auth/dto/LoginRequestDTO.java` | Move | From `infrastructure/rest/auth/` |
| `application/auth/dto/RegisterRequestDTO.java` | Move | From `infrastructure/rest/auth/` |
| `application/carrito/dto/CarritoResponseDTO.java` | Move+Simplify | Removed entity constructor |
| `application/carrito/dto/ItemCarritoDTO.java` | Move+Simplify | Removed entity constructor |
| `application/pedido/dto/DetallePedidoDTO.java` | Move | From `infrastructure/rest/pedido/` |
| `application/pedido/dto/PedidoResponseDTO.java` | Move | From `infrastructure/rest/pedido/` |
| `application/producto/dto/ProductoRequestDTO.java` | Move | From `infrastructure/rest/producto/` |
| `application/producto/dto/ProductoResponseDTO.java` | Move | From `infrastructure/rest/producto/` |
| `application/usuario/dto/UsuarioRequestDTO.java` | Move | From `infrastructure/rest/usuario/` |
| `application/usuario/dto/UsuarioResponseDTO.java` | Move | From `infrastructure/rest/usuario/` |
| 5 use case files | Modify | Updated imports from `infrastructure.rest.{modulo}` to `application.{modulo}.dto` |
| 5 controller files | Modify | Updated imports to match new DTO locations |
| 2 security files | Modify | Updated `UserDetailsImpl` import path |
| 5 test files | Modify | Updated import paths |

## Interfaces / Contracts

No new interfaces introduced. The existing interfaces and contracts remain unchanged:
- API endpoints and response shapes are identical (move preserved class names)
- Domain repository ports unchanged
- Use case public method signatures unchanged

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Compilation | All source files compile | `./mvnw clean compile` — ✅ PASS |
| Regression | All existing tests pass | `./mvnw test` — ✅ all 10 suites green |
| Import hygiene | No residual old-package references | Grep for `import com.david.ecommerce.infrastructure.rest.*` — ✅ none found |

## Migration / Rollout

No migration required. This is a compile-time structural change with no data migration, no feature flags, and no runtime impact. The single commit (4967102) atomically moves all files and updates all consumers.

## Open Questions

None — all decisions were resolved during implementation.
