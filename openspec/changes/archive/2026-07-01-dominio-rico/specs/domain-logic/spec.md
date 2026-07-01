# Delta for Domain Logic

Refactor estructural: mover reglas de negocio desde los use cases al dominio. Sin cambios de comportamiento visible al usuario.

## ADDED Requirements

### REQ-DOMAIN-001: Stock validation

Producto DEBE exponer `tieneStockSuficiente(cantidad)` y `descontarStock(cantidad)`. `descontarStock()` DEBE lanzar `StockInsuficienteException` si `stock < cantidad`. `tieneStockSuficiente()` DEBE retornar boolean sin efectos secundarios.

#### Scenario: Descuento con stock suficiente

- GIVEN un Producto con stock = 10
- WHEN se invoca `descontarStock(3)`
- THEN el stock pasa a ser 7
- AND NO lanza excepción

#### Scenario: Descuento con stock insuficiente

- GIVEN un Producto con stock = 2
- WHEN se invoca `descontarStock(5)`
- THEN se lanza `StockInsuficienteException`
- AND el stock permanece en 2

#### Scenario: Verificación sin efectos secundarios

- GIVEN un Producto con stock = 5
- WHEN se invoca `tieneStockSuficiente(3)`
- THEN retorna `true`
- AND el stock sigue siendo 5

### REQ-DOMAIN-002: Order total calculation

Pedido DEBE calcular `total` automáticamente desde sus Detalles. `setTotal()` NO DEBE ser público. `agregarDetalle()` DEBE recalcular el total.

#### Scenario: Total calculado desde detalles

- GIVEN un Pedido sin detalles
- WHEN se agrega un Detalle con subtotal = 100.00
- THEN `total` del Pedido es 100.00
- AND `setTotal()` NO es accesible públicamente

### REQ-DOMAIN-003: Order state transitions

Pedido DEBE validar transiciones de estado mediante `puedeCancelar()` / `cancelar()` y `puedePagar()` / `pagar()`. `cancelar()` DEBE lanzar excepción si estado es CANCELADO o ENTREGADO. `pagar()` DEBE lanzar excepción si estado no es PENDIENTE.

#### Scenario: Cancelación exitosa

- GIVEN un Pedido en estado PENDIENTE
- WHEN se invoca `cancelar()`
- THEN el estado pasa a CANCELADO

#### Scenario: Cancelación de pedido ya cancelado

- GIVEN un Pedido en estado CANCELADO
- WHEN se invoca `cancelar()`
- THEN se lanza `ValidacionNegocioException`

#### Scenario: Pago exitoso

- GIVEN un Pedido en estado PENDIENTE
- WHEN se invoca `pagar()`
- THEN el estado pasa a PAGADO

#### Scenario: Pago de pedido no pendiente

- GIVEN un Pedido en estado ENTREGADO
- WHEN se invoca `pagar()`
- THEN se lanza `ValidacionNegocioException`

### REQ-DOMAIN-004: Cart item ownership

Carrito DEBE exponer `contieneItem(itemId)` para verificar si un item pertenece al carrito.

#### Scenario: Item pertenece al carrito

- GIVEN un Carrito con items [1, 2, 3]
- WHEN se invoca `contieneItem(2)`
- THEN retorna `true`

#### Scenario: Item no pertenece al carrito

- GIVEN un Carrito con items [1, 2, 3]
- WHEN se invoca `contieneItem(99)`
- THEN retorna `false`

### REQ-DOMAIN-005: Backward compatibility

El refactor NO DEBE alterar el comportamiento de las APIs existentes. `mvnw test` DEBE pasar. `mvnw clean compile` DEBE pasar.

#### Scenario: Tests existentes pasan

- GIVEN el código post-refactor
- WHEN se ejecuta `mvnw test`
- THEN todos los tests pasan

#### Scenario: Compilación exitosa

- GIVEN el código post-refactor
- WHEN se ejecuta `mvnw clean compile`
- THEN la compilación es exitosa
