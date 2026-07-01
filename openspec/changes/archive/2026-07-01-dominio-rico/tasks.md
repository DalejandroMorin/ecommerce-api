# Tasks: dominio-rico

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 80–120 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

## Phase 1: Producto — Stock methods

- [x] 1.1 Agregar `tieneStockSuficiente(Integer cantidad)` a `Producto.java` — retorna `stock >= cantidad` sin efectos secundarios
- [x] 1.2 Agregar `descontarStock(Integer cantidad)` a `Producto.java` — lanza `StockInsuficienteException` si stock < cantidad, sino descuenta
- [x] 1.3 Agregar `restaurarStock(Integer cantidad)` a `Producto.java` — suma stock, sin validación de límite superior
- [x] 1.4 Cambiar `setStock(Integer stock)` a `private` en `Producto.java` (+ `cambiarStock` público para admin CRUD)

## Phase 2: Pedido — Total y transiciones

- [x] 2.1 Cambiar `setTotal(BigDecimal total)` a `private` en `Pedido.java`
- [x] 2.2 Agregar `calcularTotal()` — itera `detalles`, suma subtotales, setea `this.total`
- [x] 2.3 Modificar `agregarDetalle(Detalle detalle)` para que llame a `calcularTotal()` post-insert
- [x] 2.4 Modificar `removerDetalle(Detalle detalle)` para que llame a `calcularTotal()` post-removal
- [x] 2.5 Agregar `puedeCancelar()` — retorna `estado != CANCELADO && estado != ENTREGADO`
- [x] 2.6 Agregar `cancelar()` — valida con `puedeCancelar()`, lanza `ValidacionNegocioException` si no, setea estado a `CANCELADO`
- [x] 2.7 Agregar `puedePagar()` — retorna `estado == PENDIENTE`
- [x] 2.8 Agregar `pagar()` — valida con `puedePagar()`, lanza `ValidacionNegocioException` si no, setea estado a `PAGADO`
- [x] 2.9 Cambiar `setEstado(EstadoPedido estado)` a `private` (+ `cambiarEstado` público para admin)

## Phase 3: Carrito — Item ownership

- [x] 3.1 Agregar `contieneItem(Long itemId)` a `Carrito.java` — itera `items`, compara `getId()`

## Phase 4: Simplificar Use Cases

- [x] 4.1 `PedidoUseCase.construirPedido()` — reemplazar cálculo manual, `agregarDetalle()` llama `calcularTotal()` internamente
- [x] 4.2 `PedidoUseCase.crearPedidoDesdeCarrito()` — usar `producto.tieneStockSuficiente()` y `producto.descontarStock()`
- [x] 4.3 `PedidoUseCase.cancelarPedido()` — simplificar a `pedido.cancelar()`, usar `producto.restaurarStock()`
- [x] 4.4 `PedidoUseCase.actualizarEstado()` — usar `pedido.cambiarEstado()` en vez de `setEstado`
- [x] 4.5 `PagoSimuladoUseCase.procesarPago()` — usar `pedido.pagar()`
- [x] 4.6 `CarritoUseCase.agregarProducto()` — usar `producto.tieneStockSuficiente()`
- [x] 4.7 `CarritoUseCase.actualizarCantidad()` — usar `producto.tieneStockSuficiente()` y `carrito.contieneItem()`
- [x] 4.8 `CarritoUseCase.eliminarItem()` — usar `carrito.contieneItem()`

## Phase 5: Compile + Test

- [x] 5.1 Ejecutar `./mvnw clean compile` — BUILD SUCCESS
- [x] 5.2 Ejecutar `./mvnw test` — 125 tests, 0 failures, 55 pre-existing DB errors
