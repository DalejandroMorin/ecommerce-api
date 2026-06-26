# Proposal: mover-rest-controllers

## Intent

Terminar la migración hexagonal iniciada en `hexagonal-migration`. El módulo `producto` ya migró sus controllers y DTOs a `infrastructure/rest/producto/`. Los módulos `usuario`, `carrito`, `pedido` y `auth` aún tienen controllers + DTOs en sus packages legacy (`{modulo}/controller/`, `{modulo}/dto/`). Este cambio los mueve a `infrastructure/rest/{modulo}/` siguiendo exactamente el mismo patrón. Refactor puro: cero cambios de comportamiento, endpoints, o DB.

## Scope

### In Scope
- **Usuario**: mover controller + 2 DTOs + test a `infrastructure/rest/usuario/`
- **Carrito**: mover controller + 2 DTOs + test a `infrastructure/rest/carrito/`
- **Pedido**: mover controller + 2 DTOs + test a `infrastructure/rest/pedido/`
- **Auth**: mover controller + 3 DTOs + test a `infrastructure/rest/auth/`
- **Eliminar packages legacy**: borrar `{modulo}/controller/`, `{modulo}/dto/` tras mover

### Out of Scope
- Cambios de lógica de negocio, endpoints, o schema DB
- Refactor de `AuthUseCase`, `TokenService` u otras clases de aplicación/infra
- Nuevos tests o modificación de tests existentes (solo se mueven de package)

## Capabilities

**New Capabilities**: None — refactor puro, sin cambios de comportamiento a nivel spec.
**Modified Capabilities**: None — no hay cambios de requirements ni contractos de API.

## Approach

1. Crear `infrastructure/rest/{usuario,carrito,pedido,auth}/` con los mismos controllers y DTOs, solo cambiando el `package` (misma estructura que `infrastructure/rest/producto/`)
2. Mover tests a `src/test/java/.../infrastructure/rest/{modulo}/`
3. Eliminar packages legacy `{modulo}/controller/`, `{modulo}/dto/`
4. Verificar que `./mvnw clean test` pase sin cambios

Cada módulo se procesa secuencialmente (1 commit por módulo) para facilitar rollback.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `{usuario,carrito,pedido,auth}/controller/` | Deleted | Controllers movidos a infrastructure/rest/ |
| `{usuario,carrito,pedido,auth}/dto/` | Deleted | DTOs movidos a infrastructure/rest/ |
| `infrastructure/rest/{usuario,carrito,pedido,auth}/` | New | Destino de controllers, DTOs y tests |
| Tests legacy | Moved | De `{modulo}/controller/` a `infrastructure/rest/{modulo}/` |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Package name change rompe imports | Low | Tests + compilación detectan inmediatamente |
| Olvidar migrar test de algún módulo | Low | Revisar cada módulo tiene test en el destino |

## Rollback Plan

Commits independientes por módulo. Si un módulo falla, `git revert <commit>` sin afectar los demás.

## Dependencies

None.

## Success Criteria

- [ ] `./mvnw clean compile` compila sin errores
- [ ] `./mvnw test` pasa los 10+ tests existentes
- [ ] Cero archivos en `src/main/.../{usuario,carrito,pedido,auth}/controller/` ni `dto/`
- [ ] Todos los controllers están en `infrastructure/rest/{modulo}/`
