# Design: JPA / Adapter Integration Tests

## Technical Approach

Integration test suite covering 4 modules (Producto, Usuario, Carrito, Pedido) using Testcontainers PostgreSQL under `@DataJpaTest`. Each module gets entity lifecycle, mapper roundtrip, and adapter CRUD tests. A shared `AbstractIntegrationTest` base provides the PostgreSQL container + `@DynamicPropertySource` wiring. Edge cases (enum sync, null collections, `@PrePersist` overwrite, lazy loading) are dedicated scenarios.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|-------------|-----------|
| TC wiring | `@Testcontainers` + `@Container` static + `@DynamicPropertySource` | `@ServiceConnection` (SB 4.1) | `@DynamicPropertySource` is battle-tested; avoids snapshot TC integration risk |
| Container lifecycle | Singleton static container shared across all tests | Per-class | Shared saves ~10s per class; `@Transactional` rollback isolates state |
| Test slice | `@DataJpaTest` | `@SpringBootTest` | Loads only JPA-layer beans — faster, focused |
| Mapper tests | ProductoMapper + UsuarioMapper: pure unit. CarritoMapper + PedidoMapper: via adapter tests (real entities) | All in @DataJpaTest | Second group navigates lazy Entity graphs needing a real DB; first group does flat field mapping |
| DB cleanup | Implicit via `@DataJpaTest` `@Transactional` + `@AutoConfigureTestDatabase(replace = NONE)` | Manual `@AfterEach` | `@DataJpaTest` auto-rolls back per test — no explicit cleanup needed |
| Dialect | Explicit `spring.jpa.properties.hibernate.dialect` override | Auto-detection | Testcontainers dynamic URL may confuse auto-detection; explicit removes ambiguity |
| Test data | Inline factory methods per test class | Shared builders | Self-contained tests, no cross-test coupling |

## Class Structure

```
src/test/java/com/david/ecommerce/infrastructure/persistence/jpa/
├── AbstractIntegrationTest.java
├── entity/
│   ├── ProductoEntityTest.java        ← lifecycle, timestamps, enum mapping
│   ├── UsuarioEntityTest.java         ← lifecycle, unique constraint, rol enum
│   ├── CarritoEntityTest.java         ← cascade ALL, orphanRemoval, lazy load
│   ├── PedidoEntityTest.java          ← cascade ALL, @PrePersist, EstadoPedido
│   └── DetallePedidoEntityTest.java   ← @PrePersist subtotal overwrite
├── mapper/
│   ├── ProductoMapperTest.java        ← pure unit: roundtrip + null safety
│   └── UsuarioMapperTest.java         ← pure unit: roundtrip + null safety
└── adapter/
    ├── JpaProductoRepositoryAdapterTest.java
    ├── JpaUsuarioRepositoryAdapterTest.java
    ├── JpaCarritoRepositoryAdapterTest.java
    └── JpaPedidoRepositoryAdapterTest.java
```

## Data Flow

### Entity test

```
persist → em.flush() → em.clear() → em.find() → assert fields, timestamps, enums, cascades
```

### Adapter CRUD test

```
save(domain) → findById(id) → assert
→ findAll() → assert includes
→ save(update) → findById(id) → assert update applied
→ deleteById(id) → findById(id) → assert empty
```

### Custom query (Producto Adapter example)

```
persist 3 ProductoEntity (varying categoria, precio, stock)
→ buscarConFiltros("nombre", null, null, null, null) → 1 match
→ buscarConFiltros(null, ELECTRONICA, 10, 100, 5) → filtered
→ buscarConFiltros(null, null, null, null, 999) → empty list
→ buscarConFiltros(null, null, null, null, null) → all (EC-5 null param = no filter)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `pom.xml` | Modify | Add `org.testcontainers:postgresql` + `org.testcontainers:junit-jupiter` under TC BOM |
| `AbstractIntegrationTest.java` | Create | `@DataJpaTest` + `@Testcontainers` + `@Container` static PostgreSQL + `@DynamicPropertySource` + Hibernate dialect override |
| `ProductoEntityTest.java` | Create | 6 methods: persist-read, update timestamp, enum mapping, categoria sync (EC-1) |
| `UsuarioEntityTest.java` | Create | 5 methods: persist-read, @PrePersist fechaRegistro, unique email constraint, rol enum sync (EC-1) |
| `CarritoEntityTest.java` | Create | 3 methods: cascade persist, orphanRemoval, lazy load within txn |
| `PedidoEntityTest.java` | Create | 3 methods: cascade persist, @PrePersist fechaPedido + default estado, EstadoPedido enum sync (EC-1) |
| `DetallePedidoEntityTest.java` | Create | 1 method: @PrePersist overwrites mapper-set subtotal with precioUnitario × cantidad (EC-4) |
| `ProductoMapperTest.java` | Create | 2 methods: roundtrip preserves all fields, null input returns null |
| `UsuarioMapperTest.java` | Create | 2 methods: roundtrip preserves all fields, null input returns null |
| `JpaProductoRepositoryAdapterTest.java` | Create | 8+ methods: CRUD (5) + buscarConFiltros (2 + 1 empty + 1 null params = EC-5) |
| `JpaUsuarioRepositoryAdapterTest.java` | Create | 8+ methods: CRUD (4) + findByEmail (found + empty) + existsByEmail (true + false) |
| `JpaCarritoRepositoryAdapterTest.java` | Create | 5+ methods: buscarPorUsuarioId, guardar (create + update), eliminarPorId, buscarItemPorId + null collection (EC-2) |
| `JpaPedidoRepositoryAdapterTest.java` | Create | 5+ methods: CRUD (4) + findByUsuarioId + @PrePersist overwrite (EC-4) + null detalles (EC-3) |

## Interfaces / Contracts

No new interfaces. Tests exercise existing domain repository ports. Key contract edge cases:

- `CarritoMapper.toDomain()` navigates `entity.getUsuario().getId()` — lazy load, works within `@DataJpaTest` transaction
- `PedidoMapper.toEntity()` sets `entity.setSubtotal(domain.getSubtotal())` — `@PrePersist` overwrites → assert final value is `precioUnitario × cantidad`
- `ProductoMapper`, `UsuarioMapper`: null-safe, `valueOf` for enum sync

## Testing Strategy

| Layer | What | How |
|-------|------|-----|
| Entity | Lifecycle, callbacks, cascade, enum, constraints | EntityManager flush+clear+re-read within `@DataJpaTest` |
| Mapper (Producto, Usuario) | Field mapping, null safety | Pure JUnit 5 — construct domain object → `toEntity` → `toDomain` → assert all fields |
| Mapper (Carrito, Pedido) | Entity graph navigation, null collection safety | Within adapter tests — persist real entities, call `toDomain`, assert linked objects |
| Adapter | CRUD, custom queries, edge cases | Inject adapter + repos, pre-populate DB via `entityManager.persist()`, call adapter, assert domain results |
| Enum sync (EC-1) | All domain enum values exist in entity enum | Reflection: iterate `DomainEnum.values()`, call `EntityEnum.valueOf(name())`, assert no exception |
| @PrePersist overwrite (EC-4) | Subtotal recalculation on persist | Persist `DetallePedidoEntity` with mismatched subtotal → flush → re-read → assert `subtotal == precioUnitario × cantidad` |
| Null collections (EC-2, EC-3) | `CarritoEntity.items` / `PedidoEntity.detalles` are null | Set collection to null, call `toDomain`, assert returns empty list not NPE |

### pom.xml Changes

```xml
<!-- Testcontainers BOM -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-bom</artifactId>
    <version>1.20.4</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<!-- Within <dependencyManagement> or as plain deps: -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

### AbstractIntegrationTest skeleton

```java
@ActiveProfiles("test")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    protected TestEntityManager em;
}
```

## Migration / Rollout

No migration. All changes are additive — new test files and `pom.xml` dependency additions only.

## Open Questions

None.
