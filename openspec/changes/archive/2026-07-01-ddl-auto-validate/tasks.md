# Tasks: Safe Schema Validation (`ddl-auto-validate`)

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~4 (1 code + 3 docs/context) |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Chain strategy | size-exception |
| Delivery strategy | ask-on-risk |

Decision needed before apply: Yes
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

## Phase 1: Config Change

- [x] 1.1 **Modificar** `src/main/resources/application.properties` línea 9: `spring.jpa.hibernate.ddl-auto=update` → `validate`
- [x] 1.2 **Verificar** `./mvnw clean compile` compila sin errores

## Phase 2: Documentation

- [x] 2.1 **Actualizar** `AGENTS.md` línea 24: `ddl-auto=update` → `validate`
- [x] 2.2 **Actualizar** `AGENTS.md` línea 166: `spring.jpa.hibernate.ddl-auto=update` → `validate`

## Notas

- No requiere tests automáticos (ver design: `SchemaManagementException` es garantizada por Spring Boot/Hibernate; no hay código de aplicación que testear)
- Rollback: `git revert HEAD` o reverse manual de los 3 cambios
