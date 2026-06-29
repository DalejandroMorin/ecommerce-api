# AGENTS.md — E-Commerce API

## Build & Test Commands

```bash
./mvnw clean install                    # Full build (compile + test + package)
./mvnw spring-boot:run                  # Run app at http://localhost:8080
./mvnw test                             # Run all tests
./mvnw test -Dtest=XxxTest              # Run single test class
./mvnw test -Dtest=XxxTest#methodName   # Run single test method
./mvnw clean test                       # Clean + run tests (stale class files fix)
./mvnw compile                          # Compile only
```

**Run app**: requires PostgreSQL at `localhost:5432/ecommerce` and env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (all have defaults in `application.properties`).

## Project Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (webmvc, data-jpa, security, validation) |
| Build | Maven 3.9+ with wrapper (`mvnw`) |
| Database | PostgreSQL via JPA/Hibernate (`ddl-auto=update`) |
| Auth | JWT (jjwt 0.12.6) |
| Docs | Swagger (springdoc-openapi 2.8.5) at `/swagger-ui.html` |
| Utils | Lombok, SLF4J |
| Test | JUnit 5 + Mockito + AssertJ + MockMvc |

## Architecture

Follows **hexagonal/clean architecture**:

```
Controller → UseCase (application, @Service @Transactional)
    → Repository port (domain interface) → JPA adapter (infrastructure)
        → Spring Data JpaRepository → Entity (JPA @Entity)
```

- All modules (`producto`, `usuario`, `auth`, `carrito`, `pedido`) follow hexagonal structure:
  - `domain/{modulo}/` — domain models + repository ports
  - `application/{modulo}/` — use cases
  - `infrastructure/` — JPA adapters, security, controllers
- **Mappers** (`ProductoMapper`, `UsuarioMapper`, `CarritoMapper`, `PedidoMapper`): static methods `toEntity()` / `toDomain()` in `infrastructure/persistence/jpa/mapper/`

## Code Style Guidelines

### Language & Naming
- **Spanish** for ALL identifiers: classes, methods, variables, comments, commit messages, logs
- Classes: `PascalCase` → `ProductoController`, `RecursoNoEncontradoException`
- Methods/variables: `camelCase` → `obtenerPorId()`, `crearPedidoDesdeCarrito()`
- Constants/enums: `UPPER_SNAKE_CASE` → `PENDIENTE`, `CLIENTE`, `ELECTRONICA`
- Tests: descriptive with underscores → `crearPedido_StockInsuficiente_LanzaExcepcion`

### Formatting & Imports
- **No wildcard imports** except: `jakarta.persistence.*` on entities, `org.springframework.web.bind.annotation.*` on controllers
- Order: `java.*` → `jakarta.*` → third-party → project imports with blank lines between groups

### Lombok Usage
- **DTOs**: use `@Data @NoArgsConstructor @AllArgsConstructor`
- **JPA Entities**: use `@Data @NoArgsConstructor @AllArgsConstructor`
- **NEVER** use Lombok on: domain models (hand-write getters/setters), controllers, services/use cases, configs, exceptions, mappers

### Dependency Injection
- **Constructor injection** always — implicit for single-constructor, `@Autowired` only for clarity with multiple collaborators
- NEVER use field injection (`@Autowired` on fields)

### JPA Entities
- `@Entity @Table(name = "snake_case_plural")`
- `@Column(name = "snake_case", nullable = false, length = N, precision = 10, scale = 2)` as appropriate
- `@Enumerated(EnumType.STRING)` for enums with `@Column(length = N)`
- `@PrePersist` / `@PreUpdate` for auto-timestamping
- `fetch = FetchType.LAZY` on all `@OneToMany`, `@ManyToOne`, `@OneToOne` relationships
- `cascade = CascadeType.ALL, orphanRemoval = true` on `@OneToMany` collections

### Domain Models
- Pure Java objects in `domain/{modulo}/` — NO framework annotations, NO Lombok
- Methods: `validar()` for self-validation (called from use case), hand-written getters/setters
- Repository ports in same package: `domain/{modulo}/XxxRepository.java` interface

### Use Cases (Application Layer)
- Annotated `@Service @Transactional` in `application/{modulo}/`
- Constructor injection of domain repository ports ONLY (NOT JPA repositories)
- Method names: `obtenerXxx()`, `crearXxx()`, `actualizarXxx()`, `eliminarXxx()`
- Handle domain exceptions; map domain to DTO via `fromDomain()` static methods
- **Always call `producto.validar()` / `usuario.validar()`** for domain validation

### Error Handling
- Throw **custom exceptions** extending `BusinessException` in `common/exception/`
- Exception constructors: `(String recurso, Long id)` for not-found, `(String message)` for validation
- `GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions:
  - `RecursoNoEncontradoException` → 404
  - `StockInsuficienteException`, `CarritoVacioException`, `EmailDuplicadoException`, `ValidacionNegocioException` → 400
  - `IllegalArgumentException` → 400
  - **CRITICAL**: Add handler for `MethodArgumentNotValidException` — returns 400 with field errors for `@Valid` annotated controllers
  - `Exception` → 500
- Response body: `Map<String, Object>` with keys: `timestamp`, `status`, `error`, `message`

### Security
- All routes configured in `SecurityConfig.filterChain()`:
  - `/api/auth/**`, swagger → `permitAll()`
  - `GET /api/productos/**` → `permitAll()`
  - `POST/PUT/DELETE /api/productos/**` → `hasRole("ADMIN")`
  - `/api/usuarios/**` → `hasRole("ADMIN")`
  - `/api/pedidos/**`, `/api/carrito/**` → `authenticated()`
- JWT in `Authorization: Bearer <token>` header via `JwtAuthFilter`
- Token claims: subject=email, custom claim "rol"

### Logging
```java
private static final Logger log = LoggerFactory.getLogger(XxxService.class);
```
- `info` for successes, `warn` for deletions/cancellations/payment failures, `debug` for queries
- Always include entity ID and context; emojis optional but used (📦, 🛒, 🔐, 🗑️, ✅)

### DTO Pattern
```java
@Data @NoArgsConstructor @AllArgsConstructor
public class XxxRequestDTO {
    @NotBlank @NotNull @Min(0)  // Jakarta validation
    private String campo;
}
@Data @NoArgsConstructor @AllArgsConstructor
public class XxxResponseDTO {
    private Long id;
    // Constructor from entity: public XxxResponseDTO(XxxEntity e) { ... }
    // Factory from domain: public static XxxResponseDTO fromDomain(DomainObj d) { ... }
}
```

### JPA Repositories
- Spring Data interfaces in `infrastructure/persistence/jpa/repository/`
- `@Query` with `@Param` for custom queries (see `ProductoJpaRepository.buscarConFiltros`)
- Adapter classes in `infrastructure/persistence/jpa/adapter/` implement domain repository ports using mappers

### Controllers
- `@RestController @RequestMapping("/api/plural")`
- Use `@Valid @RequestBody` on create/update DTOs
- Return `ResponseEntity<T>` with explicit status codes: `201 Created` for create, `204 No Content` for delete
- Constructor injection of use case only

### Tests — JUnit 5 + Mockito + AssertJ + MockMvc

**Service tests** (`@ExtendWith(MockitoExtension.class)`):
```java
@Mock private XxxRepository repository;
@InjectMocks private XxxUseCase useCase;
```

**Controller tests** (standalone MockMvc):
```java
mockMvc = MockMvcBuilders.standaloneSetup(controller)
    .setValidator(new LocalValidatorFactoryBean())
    .setControllerAdvice(new GlobalExceptionHandler())
    .build();
```

- Mock repositories, NOT services (unit under isolation)
- Test naming: `methodName_Scenario_ExpectedBehavior`
- Use `@DisplayName("Descripción legible")` on test methods
- AssertJ: `assertThat(result).isNotNull()`, `assertThatThrownBy(() -> ...).isInstanceOf(XxxException.class).hasMessageContaining("texto")`

### Application Properties
- Environment variables with defaults: `${VAR:default_value}`
- Snake-case keys, all config in `application.properties` (no YAML)
- Key props: `jwt.secret`, `jwt.expiration`, `spring.datasource.*`, `spring.jpa.hibernate.ddl-auto=update`, `spring.jpa.open-in-view=false`

### Package Structure

```
com.david.ecommerce
├── application/{modulo}/        — Use cases (@Service @Transactional)
├── domain/{modulo}/             — Domain models + repository ports (no frameworks)
├── infrastructure/
│   ├── persistence/jpa/
│   │   ├── entity/              — JPA @Entity classes
│   │   ├── repository/          — Spring Data JpaRepository interfaces
│   │   ├── mapper/              — Static toEntity/toDomain mappers
│   │   └── adapter/             — Implements domain repository ports
│   └── security/                — JWT filter, UserDetails, SecurityConfig
├── {modulo}/                    — Traditional controller + dto (auth, carrito, pedido, usuario)
├── common/exception/            — BusinessException hierarchy + GlobalExceptionHandler
├── config/                      — OpenApiConfig (Swagger)
└── EcommerceApplication.java
```

### Commit Messages
- In **Spanish**, present tense, descriptive
- Format: `Tipo: descripción breve` (e.g., `Agregar migración hexagonal para producto`, `Corregir validación de stock en carrito`)

### What NOT to do
- Never commit `.env`, `logs/`, `target/`, IDE files (they are gitignored)
- Never expose secrets in code (use env vars with defaults in `application.properties`)
- Never use `@Autowired` on fields — always constructor injection
- Never add wildcard imports (except JPA/Web bindings)
- Never add `@Data` to domain models, services, controllers, configs, exceptions, or mappers
- Never inject JPA repositories or entities into use cases — always go through domain repository ports
