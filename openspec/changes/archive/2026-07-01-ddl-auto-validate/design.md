# Design: Safe Schema Validation (`ddl-auto-validate`)

## Technical Approach

Cambiar una sola property de configuración y actualizar las referencias en documentación. Sin nueva lógica, sin nuevos archivos, sin cambios de API, sin migración de datos.

1. `src/main/resources/application.properties` línea 9: `spring.jpa.hibernate.ddl-auto=update` → `validate`
2. `AGENTS.md` línea 24 (tabla de stack) y línea 166 (Application Properties): actualizar la mención de `ddl-auto=update` a `ddl-auto=validate`
3. Compilar con `./mvnw clean compile` para verificar que el proyecto sigue buildéando

## Architecture Decisions

### Decision: `validate` (no `none`, no Flyway ahora)

| Option | Tradeoff | Verdict |
|--------|----------|---------|
| `validate` | Falla al arrancar si schema ≠ entities. Fuerza migraciones explícitas. Es la opción más segura para producción sin agregar dependencias. | ✅ **Elegido** |
| `none` | Hibernate ignora el schema por completo. No hay protección contra schema drift. Riesgo alto de inconsistencias silenciosas. | ❌ Rechazado |
| `update` (status quo) | Hibernate altera tablas automáticamente. Riesgo de cambios silenciosos en producción. | ❌ Rechazado |
| Flyway/Liquibase | Migraciones versionadas y auditables. Dependencia nueva, configuración adicional, overhead de aprendizaje. | ❌ Rechazado para este cambio — queda como tarea futura independiente |

**Rationale**: `validate` es el siguiente paso lógico después de `update`. Elimina el riesgo de mutación automática del schema en producción sin requerir nueva infraestructura de migraciones. Cuando se introduzca Flyway (scope futuro), `validate` es compatible: Hibernate valida contra el schema real mientras Flyway gestiona los cambios versionados.

### Decision: Sin test automático de integración para schema mismatch

**Choice**: No agregar test de integración ahora.
**Rationale**: El comportamiento (schema mismatch → app no arranca) lo garantiza Spring Boot/Hibernate internamente. No hay código de aplicación que testear. Agregar un `@SpringBootTest` para esto requeriría una BD de test separada con schema intencionalmente corrupto — infraestructura desproporcionada para un cambio de una línea. Se confirma con `mvnw clean compile` y validación manual en entorno local.

### Decision: La app se despliega con schema pre-existente

**Rationale**: El schema de producción YA existe y está sincronizado con las entities porque `update` lo mantuvo consistente hasta ahora. Al cambiar a `validate`, el schema actual ES el source of truth. Si hay discrepancia, la app fallará al arrancar y se corregirá manualmente.

## Data Flow

No hay flujo de datos nuevo. El cambio es en la inicialización de Hibernate:

```
Spring Boot start
  → Hibernate initialize
    → ddl-auto=validate
      → ¿Schema coincide con entities?
        → Sí: app arranca normalmente
        → No: SchemaManagementException → app context fails → 500 no disponible
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/application.properties` | Modify | Línea 9: `spring.jpa.hibernate.ddl-auto=update` → `validate` |
| `AGENTS.md` | Modify | Línea 24 (tabla de stack): `ddl-auto=update` → `validate` |
| `AGENTS.md` | Modify | Línea 166 (Application Properties): `spring.jpa.hibernate.ddl-auto=update` → `validate` |

## Interfaces / Contracts

Sin cambios. No hay interfaces nuevas, endpoints modificados, contratos de API alterados, ni cambios en tipos/DTOs/dominio.

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | N/A | No hay lógica de aplicación que testear |
| Integration | Schema mismatch → app falla | Garantizado por Spring Boot + Hibernate internamente. No se agrega test automático (ver Decision). Validación manual con `mvnw clean compile` |
| E2E | N/A | Sin cambios funcionales que validar |

## Migration / Rollout

No se requiere migración de datos. El schema existente está sincronizado con las entities (producto del uso previo de `update`).

**Rollback**: `git revert <commit-hash>` o manualmente revertir los 3 cambios (1 en application.properties, 2 en AGENTS.md).

## Open Questions

Ninguna.
