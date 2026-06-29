# Design: Hexagonal Domain Consistency

## Technical Approach

Six independent fixes closing gaps found in the hexagonal architecture audit.
Commits follow dependency order: domain validation → use case fixes → infrastructure
move → docs update. TDD-first: write failing test per issue before implementing.

## Architecture Decisions

### Decision: Carrito.validar() and Pedido.validar() throw IllegalArgumentException

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Throw `CarritoVacioException` /
custom `PedidoInvalidoException` | extends `BusinessException` → 400
(already mapped), but breaks the Producto/Usuario convention | **Rejected** |
| Throw `IllegalArgumentException` | Matches `Producto.validar()` and
`Usuario.validar()` exactly; already mapped → 400 in GlobalExceptionHandler | **Chosen** |

**Rationale**: Consistency with the two existing `validar()` implementations.
Switching exception types for PedidoUseCase tests is a one-line change per test.

### Decision: UserDetailsImpl moves to infrastructure/security with package change

| Option | Tradeoff | Decision |
|--------|----------|----------|
| New package `infrastructure/security` | Aligns with hexagonal layering — Spring
Security adapter belongs in infrastructure | **Chosen** |
| Keep in `application/auth/` | Violates hexagonal boundary; same pain point the
audit flagged | **Rejected** |

**Rationale**: Pure relocation — no logic change. `AuthUseCase` and `JwtAuthFilter`
already import it; only the package qualifier changes. Four files need import updates.

## Data Flow

### AuthUseCase.register() — reordered validation

    RegisterRequestDTO ──→ existsByEmail? ──NO──→ new Usuario(raw password)
                                                      │
                                                      ├── usuario.validar()
                                                      │       │ (IllegalArgumentException if invalid)
                                                      │       ▼
                                                      ├── passwordEncoder.encode()
                                                      │       ▼
                                                      └── usuarioRepository.save()

### PedidoUseCase.crearPedidoDesdeCarrito() — domain validation

    carrito.validar() ──→ construirPedido() ──→ pedido.validar() ──→ save

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/com/david/ecommerce/domain/carrito/Carrito.java` | Modify | Add `validar()` — reject null/empty items list |
| `src/main/java/com/david/ecommerce/domain/pedido/Pedido.java` | Modify | Add `validar()` — reject null/empty detalles |
| `src/main/java/com/david/ecommerce/application/carrito/CarritoUseCase.java` | Modify | Call `carrito.validar()` and `pedido.validar()` where appropriate |
| `src/main/java/com/david/ecommerce/application/pedido/PedidoUseCase.java` | Modify | Replace inline carrito-vacio check with `carrito.validar()`; call `pedido.validar()` after building |
| `src/main/java/com/david/ecommerce/application/usuario/UsuarioUseCase.java` | Modify | Replace inline email/password validation with `usuario.validar()` |
| `src/main/java/com/david/ecommerce/application/auth/AuthUseCase.java` | Modify | Reorder: construct Usuario → `validar()` → encode password |
| `src/main/java/com/david/ecommerce/application/auth/UserDetailsImpl.java` | Delete | Moved to infrastructure |
| `src/main/java/com/david/ecommerce/infrastructure/security/UserDetailsImpl.java` | Create | New home, same content, updated package |
| `src/main/java/com/david/ecommerce/infrastructure/security/UserDetailsServiceImpl.java` | Modify | Update import of UserDetailsImpl |
| `src/main/java/com/david/ecommerce/infrastructure/security/jwt/JwtAuthFilter.java` | Modify | Update import of UserDetailsImpl |
| `src/test/java/com/david/ecommerce/auth/service/AuthUseCaseTest.java` | Modify | Update import of UserDetailsImpl |
| `src/test/java/com/david/ecommerce/usuario/service/UsuarioServiceTest.java` | Modify | Update exception expectations (ValidacionNegocioException → IllegalArgumentException) |
| `src/test/java/com/david/ecommerce/pedido/service/PedidoServiceTest.java` | Modify | Update exception expectation for empty carrito |
| `src/test/java/com/david/ecommerce/carrito/service/CarritoServiceTest.java` | Modify | Add tests for Carrito.validar() |
| `AGENTS.md` | Modify | Line 41: remove "traditional layered" for auth, usuario, carrito, pedido |
| `openspec/config.yaml` | Modify | Update `architecture:` context line to reflect all modules as hexagonal |

## Interfaces / Contracts

```java
// Carrito.java — new method
public void validar() {
    if (items == null || items.isEmpty())
        throw new IllegalArgumentException("El carrito no puede estar vacío");
}

// Pedido.java — new method
public void validar() {
    if (detalles == null || detalles.isEmpty())
        throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
    for (DetallePedido d : detalles) {
        if (d.getCantidad() == null || d.getCantidad() <= 0)
            throw new IllegalArgumentException("La cantidad del detalle debe ser positiva");
    }
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit — domain | `Carrito.validar()` empty → throws, with items → OK | Direct domain test, no mocks |
| Unit — domain | `Pedido.validar()` empty → throws, with items → OK, negative cantidad → throws | Direct domain test, no mocks |
| Unit — application | `UsuarioUseCase.crear()` delegates to `usuario.validar()` | Mock repository, assert exception type changes from `ValidacionNegocioException` to `IllegalArgumentException` |
| Unit — application | `AuthUseCase.register()` validates raw password, not encoded | Verify `passwordEncoder.encode()` is called AFTER `usuario.validar()` |
| Unit — application | `PedidoUseCase.crearPedidoDesdeCarrito()` calls `carrito.validar()` instead of inline | Update existing test expectation |
| Unit — infrastructure | `UserDetailsImpl` package moved | Import and compile check |

## Migration / Rollout

No migration required. Each fix is an independent commit:

1. **Domain models**: Carrito.validar() + Pedido.validar() + their unit tests
2. **Use case fixes**: CarritoUseCase, PedidoUseCase, UsuarioUseCase, AuthUseCase + test updates
3. **Infrastructure move**: Delete old UserDetailsImpl, create in new package, update 4 imports
4. **Docs**: AGENTS.md + openspec/config.yaml

Each commit can be reverted independently. `./mvnw clean test` must pass at every
commit boundary.

## Open Questions

- None.
