# Delta for Value Objects

## ADDED Requirements

### REQ-VO-001: Email value object

`Email` MUST be an immutable VO with constructor regex validation. MUST reject null, empty, and invalid format. MUST implement value-based equals/hashCode.

#### Scenario: Valid email accepted
- GIVEN a valid email string
- WHEN constructing `Email`
- THEN the instance is created

#### Scenario: Invalid email rejected
- GIVEN null, empty, or malformed strings
- WHEN constructing `Email`
- THEN `IllegalArgumentException` is thrown

### REQ-VO-002: Password value object

`Password` MUST be an immutable VO encapsulating a hash. `fromRaw(String)` MUST hash the raw password. `fromHash(String)` MUST reconstruct from stored hash. `matches(String)` MUST verify against stored hash.

#### Scenario: FromRaw creates hashed instance
- GIVEN a raw password string
- WHEN calling `Password.fromRaw(raw)`
- THEN hash is stored internally

#### Scenario: FromHash reconstructs from DB
- GIVEN a stored hash
- WHEN calling `Password.fromHash(hash)`
- THEN instance has that exact hash

#### Scenario: Matches verifies raw against hash
- GIVEN `Password.fromRaw("secret123")`
- WHEN calling `matches("secret123")`
- THEN returns `true`

### REQ-VO-003: Money value object

`Money` MUST be an immutable VO with `BigDecimal amount` and `Currency currency`. MUST support `add(Money)` and `multiply(BigDecimal)` validating same currency. MUST throw `IllegalArgumentException` on currency mismatch.

#### Scenario: Same-currency addition
- GIVEN `Money(10, USD)` and `Money(5, USD)`
- WHEN calling `add`
- THEN returns `Money(15, USD)`

#### Scenario: Currency mismatch on add
- GIVEN `Money(10, USD)` and `Money(5, EUR)`
- WHEN calling `add`
- THEN `IllegalArgumentException` is thrown

#### Scenario: Multiply preserves currency
- GIVEN `Money(10, USD)` and factor 3
- WHEN calling `multiply(3)`
- THEN returns `Money(30, USD)`

### REQ-VO-004: Direccion value object

`Direccion` MUST be an immutable VO with fields `calle`, `ciudad`, `codigoPostal`, `pais`. MUST reject null or blank strings for any field.

#### Scenario: Complete direccion accepted
- GIVEN 4 non-blank strings
- WHEN constructing `Direccion`
- THEN the instance is created

#### Scenario: Null field rejected
- GIVEN any field as null or blank
- WHEN constructing `Direccion`
- THEN `IllegalArgumentException` is thrown

### REQ-VO-005: Cantidad value object

`Cantidad` MUST be an immutable VO wrapping a positive integer. MUST reject null, zero, and negatives.

#### Scenario: Positive cantidad accepted
- GIVEN a positive integer
- WHEN constructing `Cantidad`
- THEN the instance is created

#### Scenario: Zero or negative rejected
- GIVEN zero or a negative integer
- WHEN constructing `Cantidad`
- THEN `IllegalArgumentException` is thrown

### REQ-VO-006: Domain model integration

Producto MUST use `Money` for precio and `Cantidad` for stock. Usuario MUST use `Email`, `Password`, `Direccion`. ItemCarrito and DetallePedido MUST use `Cantidad` and `Money`. Existing public behavior MUST NOT change — types adapt internally.

#### Scenario: Producto uses VO types
- GIVEN a Producto with `Money` and `Cantidad`
- WHEN calling `getPrecio()`, `getStock()`
- THEN VOs are returned

#### Scenario: Usuario uses VO types
- GIVEN a Usuario with `Email`, `Password`, `Direccion`
- WHEN calling VO getters
- THEN the corresponding VO instances are returned

### REQ-VO-007: JPA mapping

VOs interacting with JPA MUST be annotated `@Embeddable`. `Money` → columns `precio_monto`, `precio_moneda`. `Direccion` → columns `calle`, `ciudad`, `codigo_postal`, `pais`. `Cantidad` → replaces Integer column. `Email` → replaces String column. `Password` → single String column storing hash.

#### Scenario: Entity persists embedded VOs
- GIVEN a JPA entity with `@Embeddable` VO fields
- WHEN persisting and reading back
- THEN VO fields map to the correct columns without `@AttributeOverride`

### REQ-VO-008: Backward compatibility

REST APIs MUST accept and return the same JSON formats. Jackson MUST serialize VOs using `@JsonValue`/`@JsonCreator`. All existing tests MUST pass unchanged.

#### Scenario: JSON serialization preserves format
- GIVEN a DTO containing `Email("test@test.com")`
- WHEN serialized
- THEN JSON contains `"test@test.com"`

#### Scenario: Compilation succeeds
- GIVEN the codebase after VO migration
- WHEN running `./mvnw clean compile`
- THEN compilation succeeds

#### Scenario: Existing tests pass
- GIVEN the codebase after VO migration
- WHEN running `./mvnw test`
- THEN all tests pass
