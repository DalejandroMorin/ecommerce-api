# Proposal: Cambiar `ddl-auto` de `update` a `validate`

## Intent

Eliminar el riesgo de que Hibernate modifique el schema de PostgreSQL automáticamente en producción. Con `ddl-auto=update`, cualquier cambio en las entities JPA puede alterar tablas, columnas o constraints sin control — causando pérdida de datos, cambios silenciosos, o fallos impredecibles en deploy. Cambiar a `validate` hace que la app falle AL ARRANCAR si el schema no coincide con las entities, forzando migraciones explícitas.

## Scope

### In Scope
- Cambiar `spring.jpa.hibernate.ddl-auto=update` → `validate` en `src/main/resources/application.properties`
- Actualizar las 2 referencias a `ddl-auto=update` en `AGENTS.md` (líneas 24 y 166)

### Out of Scope
- Migraciones con Flyway/Liquibase (se hará en bloque futuro)
- Cambios en entities JPA, dominio, o cualquier otro archivo
- Tests (no hay comportamiento nuevo que testear; el cambio se valida con `mvnw compile`)

## Capabilities

### New Capabilities
None

### Modified Capabilities
None

## Approach

1. Editar `src/main/resources/application.properties` línea 9: reemplazar `update` → `validate`
2. Editar `AGENTS.md` líneas 24 y 166: reemplazar `ddl-auto=update` → `ddl-auto=validate`
3. Ejecutar `./mvnw clean compile` para verificar que compila
4. Hacer commit

No hay cambios de arquitectura, lógica de negocio, ni nuevas dependencias.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/resources/application.properties` | Modified | Línea 9: `update` → `validate` |
| `AGENTS.md` | Modified | Líneas 24 y 166: documentación actualizada |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| App no arranca en desarrollo si entities y schema difieren | Medium | Es el comportamiento deseado — revela deuda de migración. Corregir entities o schema manualmente antes de mergear |
| Developers se confunden al ver errores de schema por `validate` | Low | Documentar el cambio en AGENTS.md y en el mensaje de commit |

## Rollback Plan

Revertir el commit:

```bash
git revert HEAD
```

O manualmente:
1. `src/main/resources/application.properties` línea 9: `validate` → `update`
2. `AGENTS.md` líneas 24 y 166: `validate` → `update`

## Dependencies

Ninguna.

## Success Criteria

- [ ] `./mvnw clean compile` exitoso sin errores
- [ ] La aplicación FALLA al arrancar si hay diferencias entre entities JPA y schema PostgreSQL
- [ ] `AGENTS.md` ya no referencia `ddl-auto=update` en ningún lado
