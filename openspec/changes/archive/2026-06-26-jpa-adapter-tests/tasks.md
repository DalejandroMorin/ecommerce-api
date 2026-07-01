# Tasks: JPA / Adapter Integration Tests

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1400 |
| 400-line budget risk | High |
| Chained PRs recommended | No |
| Suggested split | Single PR (size:exception accepted) |
| Delivery strategy | single-pr |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: High

## Dependency Graph

```
Phase 1 ──┬──→ Phase 2 ──→ Phase 4
           │                       ↑
           └──→ Phase 3 ───────────┘
```

Phase 1 (infra) must come first. Phases 2 and 3 are parallel — entity tests use the base class, mapper unit tests don't need DB. Phase 4 (adapter tests) depends on both entity and mapper tests validating the underlying mapping.

## Phase 1: Infrastructure (2 tasks)

- [x] **1.1 pom.xml**: Add `testcontainers-bom:1.20.4` in `<dependencyManagement>`, then `postgresql` + `junit-jupiter` as `<scope>test</scope>` dependencies
- [x] **1.2 AbstractIntegrationTest**: `@DataJpaTest @Testcontainers @AutoConfigureTestDatabase(replace=NONE)`, static `PostgreSQLContainer` ("postgres:15-alpine"), `@DynamicPropertySource` for `spring.datasource.*` + hibernate dialect, `@Autowired TestEntityManager`

## Phase 2: Entity Tests (5 tasks)

- [x] **2.1 ProductoEntityTest** (6 methods): persist/flush/clear/find lifecycle; @PrePersist timestamps; @PreUpdate on stock change; Categoria enum STRING mapping + domain→entity enum sync via valueOf (EC-1)
- [x] **2.2 UsuarioEntityTest** (5 methods): persist-read; @PrePersist fechaRegistro; unique email constraint violation via assertThrows(DataIntegrityViolationException); Rol enum sync (EC-1)
- [x] **2.3 CarritoEntityTest** (3 methods): cascade ALL persist CarritoEntity + items; orphanRemoval after removing item from collection; lazy-load UsuarioEntity.nombre within txn
- [x] **2.4 PedidoEntityTest** (3 methods): cascade ALL persist PedidoEntity + detalles; @PrePersist fechaPedido + default PENDIENTE; EstadoPedido enum sync (EC-1)
- [x] **2.5 DetallePedidoEntityTest** (1 method): @PrePersist overwrites subtotal — persist with mismatched subtotal → flush → assert final value = precioUnitario × cantidad (EC-4)

## Phase 3: Mapper Unit Tests (2 tasks)

- [x] **3.1 ProductoMapperTest** (2 methods, pure JUnit 5): construct domain → toEntity → toDomain → assert all fields equal; null input → null
- [x] **3.2 UsuarioMapperTest** (2 methods, pure JUnit 5): construct domain → toEntity → toDomain → assert all fields equal; null input → null

## Phase 4: Adapter Tests (4 tasks)

- [x] **4.1 JpaProductoRepositoryAdapterTest** (8+ methods): CRUD (save, findById, findAll, deleteById, existsById); buscarConFiltros (match by nombre, filter by categoria+precio+stock, no-match→empty, null params→no-filter EC-5)
- [x] **4.2 JpaUsuarioRepositoryAdapterTest** (8+ methods): CRUD (save, findById, findAll, deleteById); findByEmail (found/empty); existsByEmail (true/false)
- [x] **4.3 JpaCarritoRepositoryAdapterTest** (5+ methods): buscarPorUsuarioId; guardar create vs update (orphanRemoval replaces items); eliminarPorId cascade; buscarItemPorId; null CarritoEntity.items→empty list in toDomain (EC-2)
- [x] **4.4 JpaPedidoRepositoryAdapterTest** (5+ methods): CRUD (save, findById, findAll, deleteById); findByUsuarioId; guardar with mismatched subtotal → assert @PrePersist overwrite (EC-4); null detalles→empty list in toDomain (EC-3)

## Summary

| Phase | Tasks | Files | Dependencies |
|-------|-------|-------|-------------|
| 1 — Infrastructure | 2 | pom.xml, AbstractIntegrationTest.java | None |
| 2 — Entity Tests | 5 | 5 entity test classes | Phase 1 |
| 3 — Mapper Unit Tests | 2 | ProductoMapperTest, UsuarioMapperTest | None (pure JUnit) |
| 4 — Adapter Tests | 4 | 4 adapter test classes | Phase 1 + 2 + 3 |
| **Total** | **13** | **13 files (1 modified + 12 new)** | |
