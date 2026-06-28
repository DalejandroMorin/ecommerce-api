# Proposal: JPA / Adapter Tests

## Intent

Cover the JPA layer (Entity lifecycle + Mapper + Adapter) with integration tests against a real PostgreSQL DB — level 2 of the test pyramid. The project currently has unit and controller tests but zero JPA/integration coverage, leaving mapping bugs, lazy-loading issues, and enum-sync drift to runtime.

## Scope

### In Scope
- Testcontainers PostgreSQL setup for `@DataJpaTest` (pom.xml + base config)
- Producto Entity lifecycle, Mapper roundtrip, Adapter CRUD + `buscarConFiltros`
- Usuario Entity lifecycle, Mapper roundtrip, Adapter CRUD + `findByEmail`/`existsByEmail`
- Carrito Entity (+ ItemCarrito) lifecycle, Mapper roundtrip (with real entity params), Adapter CRUD
- Pedido Entity (+ DetallePedido) lifecycle, Mapper roundtrip (with real entity params), Adapter CRUD
- Edge cases: enum sync, lazy-loading in `toDomain`, null collections, create vs update, `@PrePersist`/`@PreUpdate` timestamps

### Out of Scope
- Auth module (delegates to Usuario, no own entity)
- Standalone mapper unit tests (covered inline in adapter tests)
- Controller integration tests (`@WebMvcTest` exists)
- Production code changes (only pom.xml + new test files)
- Performance or load tests

## Capabilities

No product capabilities added or modified — pure test/infrastructure improvement.

- **New Capabilities**: None
- **Modified Capabilities**: None

## Approach

1. **pom.xml**: Add `org.testcontainers:postgresql:1.20.4`, `org.testcontainers:junit-jupiter`, and `org.springframework.boot:spring-boot-testcontainers` (if not managed by the BOM).
2. **Test base** (`AbstractJpaTest`): `@DataJpaTest` + `@Testcontainers` + `@ServiceConnection` for PostgreSQL container. Avoid `@SpringBootTest` — keep slice focused.
3. **Per-module test classes**: Each covers:
   - **Entity**: persist, read, update timestamps, enum mapping, cascade behavior
   - **Mapper**: `toEntity` + `toDomain` roundtrip via real persisted entities
   - **Adapter**: full CRUD, custom query methods, create vs update path, null/empty edge cases
4. **Data setup**: Inline test factories (no separate builders) to keep tests self-contained.
5. **The `@PrePersist` interaction** in `DetallePedidoEntity` that may overwrite mapper-set `subtotal` — explicit scenario to document the behavior.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `pom.xml` | Modified | Add Testcontainers + tc-jdbc dependencies |
| `src/test/java/.../infrastructure/persistence/jpa/` | New | All test classes (entity, mapper, adapter per module) |
| `src/test/resources/` | New (optional) | Test profile properties if needed |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Docker daemon required for Testcontainers | High | Document prerequisite; tests skip gracefully if unavailable |
| Spring Boot 4.1 snapshot incompatibility with Testcontainers | Low | Use compatible TC version; pin via `testcontainers-bom` |
| Lazy-loading `LazyInitializationException` in adapter tests | Medium | Use `@DataJpaTest` default `spring.jpa.open-in-view=true` or eager-fetch for test entities |

## Rollback Plan

Revert `pom.xml` changes and delete `openspec/changes/jpa-adapter-tests/` — no production code is touched, so rollback is zero-risk.

## Dependencies

- Docker Desktop (or equivalent) running for Testcontainers
- `org.testcontainers:postgresql` (to be added to pom.xml)

## Success Criteria

- [ ] `./mvnw test` green with all new tests passing against Testcontainers PostgreSQL
- [ ] Each adapter: CRUD + custom query methods tested
- [ ] Edge cases (null collections, enum sync, create vs update, `@PrePersist` overwrite) exercised
- [ ] No production code modified
