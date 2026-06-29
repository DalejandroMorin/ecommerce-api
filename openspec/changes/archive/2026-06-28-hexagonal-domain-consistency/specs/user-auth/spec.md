# Delta for User Auth

## ADDED Requirements

### Requirement: UsuarioUseCase delegates domain validation to Usuario.validar()

The `UsuarioUseCase.crear()` method MUST construct the `Usuario` object and call `usuario.validar()` for email format and password length validation, instead of duplicating these checks inline. Duplicate email check (`existsByEmail`) SHALL remain in the use case as it is an application-level concern requiring a repository query.

#### Scenario: Valid usuario creation uses domain validation

- GIVEN a valid `UsuarioRequestDTO`
- WHEN `UsuarioUseCase.crear()` is invoked
- THEN the `Usuario` is constructed, `usuario.validar()` is called, and the validated user is persisted

#### Scenario: Invalid email is caught by domain model

- GIVEN a `UsuarioRequestDTO` with an email missing the `@` symbol
- WHEN `UsuarioUseCase.crear()` is invoked
- THEN `usuario.validar()` throws `IllegalArgumentException` and the user is NOT persisted

#### Scenario: Short password is caught by domain model

- GIVEN a `UsuarioRequestDTO` with a password shorter than 6 characters
- WHEN `UsuarioUseCase.crear()` is invoked
- THEN `usuario.validar()` throws `IllegalArgumentException` and the user is NOT persisted

### Requirement: AuthUseCase.register() validates before encoding password

The `AuthUseCase.register()` method MUST construct the `Usuario` with the raw (unencoded) password, call `usuario.validar()` to validate the raw input, and only THEN encode the password with `PasswordEncoder` before persisting. This ensures domain validation rules (e.g. minimum password length) operate on the original user-provided value.

#### Scenario: Register with valid data validates raw password then encodes

- GIVEN a valid `RegisterRequestDTO` with a password that meets all length requirements
- WHEN `register()` is invoked
- THEN `usuario.validar()` succeeds on the raw password, `passwordEncoder.encode()` is called AFTER validation, and the encoded password is persisted

#### Scenario: Register with short password is rejected before encoding

- GIVEN a `RegisterRequestDTO` with a password shorter than 6 characters
- WHEN `register()` is invoked
- THEN `usuario.validar()` throws `IllegalArgumentException`, the password is NEVER encoded (no call to `passwordEncoder.encode()`), and no user is persisted
