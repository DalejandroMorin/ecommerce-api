# Proposal: Value Objects

## Intent

Eliminar Primitive Obsession reemplazando `String`/`Integer`/`BigDecimal` en el dominio por Value Objects con validación y semántica de negocio. Sin VOs, datos inválidos (`email` sin `@`, `stock` negativo, `precio` sin moneda) pueden atravesar capas hasta persistencia o APIs.

## Scope

### In Scope
- 5 VOs inmutables en `domain/common/`: `Email`, `Password`, `Money`, `Direccion`, `Cantidad`
- Refactor de 4 domain models: `Producto`, `Usuario`, `ItemCarrito`, `DetallePedido`
- Entities JPA: VOs como `@Embeddable` o columnas planas
- Mappers, DTOs, use cases, controllers y tests adaptados

### Out of Scope
- Cambios en schema DB (VOs embeddables = columnas existentes)
- Multi-moneda real (Money acepta Currency, sin lógica de conversión)
- Múltiples direcciones por usuario
- `@Version` en entities (ya hecho)
- Aggregate Roots (Bloque 3 futuro)

## Capabilities

### New Capabilities
None

### Modified Capabilities
None

## Approach

1. Crear 5 VOs inmutables en `domain/common/` con validación en constructor
2. Refactorizar domain models: campos primitivos → VOs, ajustar `validar()`
3. Mapear VOs como `@Embeddable` en entities JPA
4. Actualizar mappers (`toEntity()`/`toDomain()`)
5. Ajustar DTOs (VOs como campos o strings planos para JSON)
6. Adaptar use cases, controllers y tests

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `domain/common/{Email,Password,Money,Direccion,Cantidad}.java` | New | 5 VOs inmutables |
| `domain/producto/Producto.java` | Modified | precio→Money, stock→Cantidad |
| `domain/usuario/Usuario.java` | Modified | email→Email, password→Password, direccion→Direccion |
| `domain/carrito/ItemCarrito.java` | Modified | cantidad→Cantidad, precioUnitario→Money |
| `domain/pedido/DetallePedido.java` | Modified | cantidad→Cantidad, precioUnitario→Money |
| `domain/*/*Repository.java` | Modified | Firmas con VOs |
| `application/*/*UseCase.java` | Modified | Tipos actualizados |
| `infrastructure/persistence/jpa/entity/*.java` | Modified | VOs como @Embeddable |
| `infrastructure/persistence/jpa/mapper/*.java` | Modified | Mapping VO↔Entity |
| DTOs + Controllers | Modified | Serialización de VOs |
| Tests | Modified | Tipos actualizados |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Rotura serialización JSON | Med | VOs expuestos como strings planos en DTOs |
| @Embeddable cambia columnas DB | Bajo | Mismas columnas sin @AttributeOverride |
| Password VO altera hash | Bajo | Factory method encapsula el algoritmo |

## Rollback Plan

`git revert` del commit del cambio. Sin migración de datos ni schema changes, rollback es limpio.

## Dependencies

- Ninguna externa. Depende del estado actual post-`dominio-rico`.

## Success Criteria

- [ ] `./mvnw clean compile` pasa sin errores
- [ ] `./mvnw test` pasa (tests actualizados con VOs)
- [ ] Todos los VOs rechazan valores inválidos en constructor
- [ ] Domain models usan VOs en lugar de primitivos
- [ ] Entities JPA persisten VOs como embeddables
