# Delta Spec: Safe Schema Validation

> **Change:** `ddl-auto-validate`
> **Module:** `deploy-config`
> **Status:** Draft

## Overview

Cambiar `spring.jpa.hibernate.ddl-auto=update` → `validate` en la configuración de Spring
Boot. Esto elimina el riesgo de que Hibernate modifique el schema de PostgreSQL
automáticamente en producción. Con `validate`, la aplicación falla al arrancar si hay
discrepancias entre las entities JPA y el schema real, forzando migraciones explícitas.

No hay nuevas capacidades funcionales. El cambio es puramente operativo/de configuración.

---

## ADDED Requirements

### REQ-DEPLOY-001: Safe Schema Validation

| Attribute | Value |
|-----------|-------|
| **ID** | REQ-DEPLOY-001 |
| **Title** | Safe Schema Validation |
| **Priority** | High |
| **Stability** | Stable |
| **Source** | Operational — seguridad en deploy |

**Description**

El sistema DEBE configurar `spring.jpa.hibernate.ddl-auto=validate`. Si hay
discrepancia entre las entities JPA y el schema de la base de datos, la aplicación
NO DEBE inicializar el contexto de Spring (fallo al arranque).

#### Scenarios

**Scenario 1: Schema matches — app starts normally**

```
Given: la base de datos PostgreSQL tiene un schema que coincide exactamente
       con las entities JPA (tablas, columnas, tipos, constraints)
 When: la aplicación se inicia
 Then: Spring Boot arranca correctamente
  And: no hay errores de validación de schema
  And: la aplicación está disponible en el puerto configurado
```

**Scenario 2: Schema mismatch — app fails to start with clear error**

```
Given: la base de datos PostgreSQL tiene un schema que NO coincide con las
       entities JPA (columna faltante, tipo incorrecto, constraint ausente)
 When: la aplicación se inicia
 Then: Spring Boot falla al arrancar
  And: el log contiene un mensaje de error claro (e.g.
       "org.hibernate.tool.schema.spi.SchemaManagementException:
        Schema-validation: missing table [xxx]")
  And: la aplicación NO está disponible en ningún puerto
```

**Scenario 3: Development workflow — manual schema management**

```
Given: un desarrollador necesita agregar una columna a una entity JPA
 When: el desarrollador modifica la entity en Java
 Then: el desarrollador DEBE actualizar el schema de PostgreSQL manualmente
       (ALTER TABLE, migration script) o mediante una herramienta de
       migraciones (Flyway, Liquibase, etc.)
  And: la aplicación NO actualiza el schema automáticamente
```

---

## MODIFIED Requirements

None.

---

## REMOVED Requirements

None.

---

## RENAMED Requirements

None.

---

## Traceability

| Req ID | Title | Source |
|--------|-------|--------|
| REQ-DEPLOY-001 | Safe Schema Validation | Proposal: `ddl-auto-validate` |
