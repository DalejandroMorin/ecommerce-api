# Spec: JPA / Adapter Integration Tests

## Purpose

Define test scenarios for JPA Entity lifecycle, Mapper roundtrip, and Adapter CRUD across 4 modules using Testcontainers PostgreSQL. Covers edge cases: enum sync, lazy loading, null collections, create vs update, and @PrePersist overwrite.

## Infrastructure

| ID | Requirement | Scenarios |
|----|------------|-----------|
| TC-1 | Test base class MUST provide @DataJpaTest + @Testcontainers + @ServiceConnection with PostgreSQL | 1 |
| TC-2 | Testcontainers SHALL use a reusable, fixed container name across test classes | 1 |
| TC-3 | Tests MUST skip gracefully when Docker is unavailable | 1 |

## Module: Producto

| ID | Requirement | Scenarios |
|----|------------|-----------|
| PE-1 | Entity lifecycle MUST persist, read by ID, and update stock/timestamps | 3 |
| PE-2 | @PrePersist MUST set fechaCreacion and fechaActualizacion on insert | 1 |
| PE-3 | @PreUpdate MUST update fechaActualizacion on update | 1 |
| PE-4 | Categoria enum MUST persist as STRING and read back correctly | 1 |
| PM-1 | Mapper toEntity then toDomain roundtrip MUST preserve all fields | 1 |
| PM-2 | Mapper MUST return null when given null input | 1 |
| PA-1 | Adapter CRUD (save, findById, findAll, deleteById, existsById) MUST work | 4 |
| PA-2 | Adapter buscarConFiltros MUST filter by any combination of params | 2 |
| PA-3 | buscarConFiltros with no match MUST return empty list | 1 |

## Module: Usuario

| ID | Requirement | Scenarios |
|----|------------|-----------|
| UE-1 | Entity lifecycle MUST persist, read by ID, and update email | 3 |
| UE-2 | @PrePersist MUST set fechaRegistro on insert | 1 |
| UE-3 | Unique constraint on email MUST reject duplicate emails | 1 |
| UM-1 | Mapper roundtrip toEntity/toDomain MUST preserve all fields | 1 |
| UM-2 | Mapper MUST return null when given null input | 1 |
| UA-1 | Adapter CRUD (save, findById, findAll, deleteById) MUST work | 4 |
| UA-2 | findByEmail MUST return Optional with found user or empty | 2 |
| UA-3 | existsByEmail MUST return true/false correctly | 2 |

## Module: Carrito

| ID | Requirement | Scenarios |
|----|------------|-----------|
| CE-1 | Cascade ALL on items MUST persist CarritoEntity + ItemCarritoEntity children together | 1 |
| CE-2 | OrphanRemoval MUST delete child items removed from the collection | 1 |
| CE-3 | FetchType.LAZY on usuario and items MUST NOT cause LazyInitializationException within @DataJpaTest | 1 |
| CM-1 | toDomain SHALL navigate lazy UsuarioEntity and items to populate domain objects | 1 |
| CM-2 | toEntity SHALL accept real entity instances (UsuarioEntity, List<ItemCarritoEntity>) | 1 |
| CM-3 | Mapper MUST handle null CarritoEntity.items by returning empty list (not NPE) | 1 |
| CA-1 | Adapter buscarPorUsuarioId MUST return cart for existing user | 1 |
| CA-2 | guardar with null id (create) MUST insert new cart + items | 1 |
| CA-3 | guardar with existing id (update) MUST replace items via orphan removal | 1 |
| CA-4 | eliminarPorId MUST cascade-delete items | 1 |
| CA-5 | buscarItemPorId MUST return individual item by item id | 1 |

## Module: Pedido

| ID | Requirement | Scenarios |
|----|------------|-----------|
| PDE-1 | Cascade ALL MUST persist PedidoEntity + DetallePedidoEntity children together | 1 |
| PDE-2 | @PrePersist MUST set fechaPedido and default PENDIENTE estado | 1 |
| PDE-3 | @PrePersist on DetallePedidoEntity MUST recalculate subtotal = unitPrice × cantidad | 1 |
| PDM-1 | toDomain SHALL navigate lazy UsuarioEntity and detalles to populate domain objects | 1 |
| PDM-2 | toEntity SHALL accept real entity instances (UsuarioEntity, List<DetallePedidoEntity>) | 1 |
| PDM-3 | @PrePersist SHALL overwrite mapper-set subtotal with calculated value on persist | 1 |
| PDA-1 | Adapter CRUD (save, findById, findAll, deleteById) MUST work | 4 |
| PDA-2 | save with null id (create) MUST insert new pedido + detalles | 1 |
| PDA-3 | save with existing id (update) MUST merge existing pedido | 1 |
| PDA-4 | findByUsuarioId MUST return all pedidos for given user | 1 |

## Edge Cases

| ID | Edge Case | Scenario |
|----|-----------|----------|
| EC-1 | Enum sync: For each module, all domain enum values MUST have a matching entity enum entry via valueOf | 3 |
| EC-2 | Null collection on CarritoEntity.items SHALL NOT throw NPE in CarritoMapper.toDomain | 1 |
| EC-3 | Null collection on PedidoEntity.detalles SHALL NOT throw NPE in PedidoMapper.toDomain | 1 |
| EC-4 | DetallePedidoEntity @PrePersist overwrite: persisted subtotal MUST equal unitPrice × cantidad regardless of mapper-set value | 1 |
| EC-5 | Null params in buscarConFiltros MUST be treated as "no filter" for that criterion | 1 |
