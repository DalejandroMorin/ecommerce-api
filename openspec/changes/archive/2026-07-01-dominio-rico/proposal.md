# Proposal: dominio-rico

## Intent

Mover lógica de negocio desde los use cases a los objetos de dominio para eliminar el **Anemic Domain Model**. Centraliza reglas, reduce duplicación y alinea el código con el lenguaje ubicuo.

## Scope

### In Scope
- **Producto.java**: agregar `tieneStockSuficiente()`, `descontarStock()`, `restaurarStock()`
- **Pedido.java**: `calcularTotal()`, `puedeCancelar()`, `cancelar()`, `puedePagar()`, `pagar()`; `setTotal` → private; `agregarDetalle()` recalcula automáticamente
- **Carrito.java**: agregar `contieneItem()`
- **PedidoUseCase.java**: reemplazar lógica inline por llamadas a nuevos métodos
- **PagoSimuladoUseCase.java**: usar `pedido.puedePagar()` / `pedido.pagar()`
- **CarritoUseCase.java**: usar `producto.tieneStockSuficiente()` y `carrito.contieneItem()`

### Out of Scope
- Value Objects (Email, Money, Direccion) — PR3
- `@Data` en JPA entities — Bloque 2
- `@Version` para optimistic locking — Bloque 2
- Cambios en JPA entities, mappers o adapters

## Capabilities

### New Capabilities
None — no nuevas funcionalidades para el usuario.

### Modified Capabilities
None — solo cambia implementación interna, no requirements a nivel spec.

## Approach

**Richening gradual** — agregar métodos al dominio, luego refactorizar use cases para delegar.

- **Producto.java**: `tieneStockSuficiente(cantidad)` → `stock >= cantidad`; `descontarStock()` reduce con validación; `restaurarStock()` incrementa.
- **Pedido.java**: `calcularTotal()` itera detalles y actualiza `total`; `setTotal` → private; `agregarDetalle()` recalcula automáticamente; `puedeCancelar()` / `cancelar()`; `puedePagar()` / `pagar()`.
- **Carrito.java**: `contieneItem(itemId)` → anyMatch sobre items.
- **PedidoUseCase.java**: delegar todo a los nuevos métodos.
- **PagoSimuladoUseCase.java**: usar `puedePagar()` + `pagar()`.
- **CarritoUseCase.java**: usar `tieneStockSuficiente()` + `contieneItem()`.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `domain/producto/Producto.java` | Modified | +3 métodos de stock |
| `domain/pedido/Pedido.java` | Modified | +6 métodos, `setTotal` → private |
| `domain/carrito/Carrito.java` | Modified | +1 método `contieneItem()` |
| `application/pedido/PedidoUseCase.java` | Modified | Delegar a dominio, ~40% menos lógica inline |
| `application/pedido/PagoSimuladoUseCase.java` | Modified | Usar `puedePagar()` / `pagar()` |
| `application/carrito/CarritoUseCase.java` | Modified | Usar `tieneStockSuficiente()` / `contieneItem()` |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Romper validación existente sin tests que la cubran | Low | Tests existentes en `PedidoUseCaseTest` y `CarritoUseCaseTest` |
| `setTotal` private rompe compilación en otros módulos | Low | Solo lo usa `PedidoUseCase.construirPedido()` |

## Rollback Plan

Revertir commits de dominio-rico con `git revert`. No hay cambios en schema de BD ni en API pública, el rollback es seguro.

## Dependencies

Ninguna — cambio autónomo sin dependencias externas.

## Success Criteria

- [ ] `./mvnw clean test` pasa sin fallos
- [ ] Lógica de stock, total, cancelación y pago delegada al dominio (ninguna regla inline en use cases)
- [ ] `setTotal` es private; `getTotal` sigue siendo público
- [ ] Tests existentes verifican el mismo comportamiento post-refactor
