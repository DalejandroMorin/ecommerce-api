# Delta for Domain Models

## ADDED Requirements

### Requirement: Carrito domain validation

The `Carrito` domain model MUST implement a `validar()` method that enforces its business invariant: a carrito MUST contain at least one `ItemCarrito`. The method MUST throw `IllegalArgumentException` following the same convention as `Producto.validar()` and `Usuario.validar()`.

#### Scenario: Empty carrito is rejected

- GIVEN a `Carrito` with an empty items list
- WHEN `validar()` is called
- THEN an `IllegalArgumentException` is thrown with a message indicating the carrito cannot be empty

#### Scenario: Carrito with items passes validation

- GIVEN a `Carrito` with at least one `ItemCarrito`
- WHEN `validar()` is called
- THEN no exception is thrown

### Requirement: Pedido domain validation

The `Pedido` domain model MUST implement a `validar()` method that enforces its business invariants: a pedido MUST contain at least one `DetallePedido`, and each detalle MUST have a positive `cantidad`. The method MUST throw `IllegalArgumentException` following the same convention as `Producto.validar()` and `Usuario.validar()`.

#### Scenario: Empty pedido is rejected

- GIVEN a `Pedido` with no detalles
- WHEN `validar()` is called
- THEN an `IllegalArgumentException` is thrown with a message indicating the pedido must have at least one item

#### Scenario: Pedido with valid items passes validation

- GIVEN a `Pedido` with at least one `DetallePedido` and each detalle has a positive `cantidad`
- WHEN `validar()` is called
- THEN no exception is thrown

### Requirement: validar() invoked from use cases

The use case for each domain model MUST call `validar()` immediately after constructing or modifying the domain object, before persisting or further processing.

#### Scenario: Use case delegates validation to domain

- GIVEN a use case constructing a `Carrito` or `Pedido`
- WHEN the object is built with user-provided data
- THEN `validar()` MUST be called before any repository operation
