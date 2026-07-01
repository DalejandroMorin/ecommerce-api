# Delta for domain-refactor

## ADDED Requirements

### Requirement: REQ-REFACTOR-001 — Single Source of Truth for Enums

Los enums `Categoria`, `Rol`, y `EstadoPedido` DEBEN definirse UNA SOLA VEZ en `domain/common/`. Ninguna otra ubicación DEBE contener una definición duplicada de estos enums.

#### Scenario: Todos los enums viven en domain/common/

- GIVEN las definiciones actuales de `Categoria`, `Rol`, y `EstadoPedido` en los pares domain/entity
- WHEN se complete el refactor
- THEN cada enum DEBE existir únicamente en `domain/common/{Enum}.java`
- AND las 6 definiciones inline originales DEBEN haber sido eliminadas

### Requirement: REQ-REFACTOR-002 — Compiler Safety

El refactor NO DEBE alterar el comportamiento en runtime. La validación es por compilación: `./mvnw clean compile` DEBE pasar sin errores. Todos los tests existentes DEBEN pasar.

#### Scenario: Compilación limpia post-refactor

- GIVEN el refactor completado
- WHEN se ejecuta `./mvnw clean compile`
- THEN el build DEBE completar sin errores de compilación

#### Scenario: Tests existentes pasan

- GIVEN el refactor completado
- WHEN se ejecuta `./mvnw test`
- THEN todos los tests existentes DEBEN pasar (green)

### Requirement: REQ-REFACTOR-003 — Backward Compatibility

La serialización JSON de enums NO DEBE cambiar (Jackson usa `name()`). Los endpoints de API DEBEN aceptar y devolver los mismos valores de enum que antes del refactor.

#### Scenario: Serialización JSON consistente

- GIVEN un endpoint que recibe o devuelve un enum
- WHEN el refactor está completo
- THEN los valores JSON aceptados y devueltos DEBEN ser idénticos a los previos al refactor
- AND Jackson DEBE seguir serializando por `name()` sin cambios de casing

## MODIFIED Requirements

None.

## REMOVED Requirements

None.

## RENAMED Requirements

None.
