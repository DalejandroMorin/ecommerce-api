# Proposal: Migración Hexagonal Completa

## Intent

Eliminar la deuda técnica de tener dos arquitecturas coexistiendo (capas tradicional + hexagonal). Unificar todos los módulos — producto, usuario, carrito, pedido, auth — bajo el patrón hexagonal/limpio estandarizado. Refactor puro: sin cambios de comportamiento ni contrato de API.

## Scope

### In Scope
- **Limpiar `producto/` legacy**: borrar controller, DTOs, entity, repository viejos (ya existe versión hexagonal completa)
- **Migrar controllers + DTOs**: mover `usuario/`, `carrito/`, `pedido/`, `auth/` a `infrastructure/rest/{modulo}/`
- **Auth**: migrar controller/DTOs + mejorar `AuthUseCase` (separar responsabilidades registro/login) y revisar puertos `TokenService`/`JwtService`
- **Tests nuevos**: JUnit 5 + Mockito + MockMvc para controllers, use cases, adapters en todos los módulos
- **Eliminar tests legacy**: tests antiguos en old package structure

### Out of Scope
- Cambios de lógica de negocio o comportamiento de API
- Nuevas funcionalidades o endpoints
- Cambios de schema DB o migración de base de datos
- Refactor de infraestructura no relacionada (Swagger, etc.)

## Capabilities

**New Capabilities**: None — refactor puro, sin cambios de comportamiento a nivel spec.
**Modified Capabilities**: None — no hay cambios de requirements.

## Approach

```
domain/{modulo}/ → application/{modulo}/ → infrastructure/
  (modelos+puertos)  (casos de uso @Service)  ├─ persistence/jpa/   (existente)
                                               ├─ rest/{modulo}/     ← NUEVO
                                               └─ security/          (existente)
```

1. **Producto cleanup**: eliminar `producto/controller/`, `dto/`, `model/`, `repository/`
2. **REST adapters**: crear `infrastructure/rest/{producto,usuario,carrito,pedido,auth}/` controllers + DTOs. Mismo patrón que `ProductoController` (constructor injection, `@Valid`, `ResponseEntity`)
3. **Auth**: mover controller/DTOs a `infrastructure/rest/auth/`. Refinar `AuthUseCase`: extraer validación de dominio, separar register/login logic. Evaluar si `TokenService` necesita interface más limpia
4. **Tests**: MockMvc standalone para controllers, Mockito para use cases. Tests por módulo en `src/test/java/com/david/ecommerce/infrastructure/rest/{modulo}/`
5. **No romper API**: mismos endpoints (`/api/...`), mismos DTOs (solo cambia package), misma response structure

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `producto/controller|dto|model|repository/` | Delete | Stale — reemplazado por hexagonal |
| `usuario/controller|dto/` | Move | → `infrastructure/rest/usuario/` |
| `carrito/controller|dto/` | Move | → `infrastructure/rest/carrito/` |
| `pedido/controller|dto/` | Move | → `infrastructure/rest/pedido/` |
| `auth/controller|dto/` | Move+Improve | → `infrastructure/rest/auth/` |
| `application/auth/AuthUseCase.java` | Modify | Refactor responsabilidades |
| `infrastructure/security/` | Modify | Revisar puertos TokenService/JwtService |
| `src/test/**/*Test.java` | Modify | Nuevos tests hexagonales, eliminar legacy |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| API contract break por cambio de package | Low | Tests existentes + nuevos validan contract |
| Auth refactor introduce bug de seguridad | Med | Tests de auth dedicados + revisión manual |
| Migration muy grande (>400 líneas diff) | High | PRs encadenados por módulo (producto cleanup → usuario → carrito → pedido → auth → tests) |
| Merge conflicts con PRs paralelos | Low | Coordinar secuencia; cambios son solo de estructura |

## Rollback

Commits independientes por módulo. Si un módulo falla, revertir solo ese commit. PRs encadenados permiten rollback granular sin afectar módulos ya migrados.

## Dependencies

None — no requiere cambios externos, DB, o infraestructura.

## Success Criteria

- [ ] `./mvnw clean test` pasa todos los tests (nuevos + existentes)
- [ ] Todos los endpoints responden igual (mismos paths, métodos, status codes, response bodies)
- [ ] Cero archivos en packages `producto/`, `usuario/`, `carrito/`, `pedido/`, `auth/` a nivel raíz
- [ ] Cobertura de tests comparable o superior a la actual (10 archivos → 15+)
