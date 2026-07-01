# Design: dominio-rico

## Technical Approach

Mover lógica de negocio desde los use cases hacia los objetos de dominio (Producto, Pedido, Carrito) agregando métodos con comportamiento real. Los use cases se simplifican a orquestación: validar entrada, cargar agregados, delegar al dominio, persistir.

## Architecture Decisions

### Decision 1: Excepciones del dominio — específicas vs. genéricas

| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| `IllegalArgumentException` (consistente con `validar()`) | No acopla dominio a `common/exception/`, pero pierde semántica | ❌ Rechazado |
| `StockInsuficienteException` / `ValidacionNegocioException` | Acopla dominio a `common/exception/` (mismo paquete compartido, no es application layer) | ✅ Elegido |

**Rationale**: La spec (REQ-DOMAIN-001) exige explícitamente `StockInsuficienteException`. Además, el handler global ya mapea estas excepciones a 400 y los tests existentes verifican el tipo exacto. `common/exception/` es un paquete compartido, no de aplicación — el dominio no gana nada lanzando IAE cuando existe una excepción semántica.

### Decision 2: `calcularTotal()` llamado desde `agregarDetalle()` y `removerDetalle()`

**Choice**: Cada modificación de la lista recalcula el total automáticamente.
**Alternativa**: Calcular bajo demanda en getter o solo al persistir.
**Rationale**: El dominio no puede mentir — el total siempre refleja los detalles. Sigue el principio de invarianza del agregado.

### Decision 3: Transiciones de estado encapsuladas

**Choice**: `setEstado()` se vuelve `private`. `cancelar()` y `pagar()` validan y mutan. `puedeCancelar()` y `puedePagar()` exponen predicados sin efectos secundarios.
**Rationale**: Elimina la posibilidad de mutación inválida desde cualquier lugar. El estado del pedido solo cambia por operaciones de dominio con significado de negocio.

## Data Flow

```
UseCase ──→ Domain method ──→ mutación interna
    │              │
    │              └── lanza excepción si invariante violada
    │
    └── si OK → persiste vía repositorio
```

```
Producto.descontarStock(3) → if stock < 3 → StockInsuficienteException
                          → else stock -= 3
Pedido.cancelar()         → if CANCELADO/ENTREGADO → ValidacionNegocioException
                          → else estado = CANCELADO
Pedido.pagar()            → if != PENDIENTE → ValidacionNegocioException
                          → else estado = PAGADO
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `domain/producto/Producto.java` | Modify | +`tieneStockSuficiente(cantidad)`, `descontarStock(cantidad)`, `restaurarStock(cantidad)`. `setStock` → private. |
| `domain/pedido/Pedido.java` | Modify | +`calcularTotal()`, `puedeCancelar()`, `cancelar()`, `puedePagar()`, `pagar()`. `setTotal` / `setEstado` → private. `agregarDetalle` y `removerDetalle` llaman `calcularTotal()`. |
| `domain/carrito/Carrito.java` | Modify | +`contieneItem(itemId)` → boolean. |
| `application/pedido/PedidoUseCase.java` | Modify | `construirPedido()` delega total a dominio. `cancelarPedido()` usa `pedido.puedeCancelar()` + `pedido.cancelar()`. Eliminar lógica inline de stock y estado. |
| `application/pedido/PagoSimuladoUseCase.java` | Modify | Usar `pedido.puedePagar()` + `pedido.pagar()`. |
| `application/carrito/CarritoUseCase.java` | Modify | Stock check via `producto.tieneStockSuficiente()`. Ownership check via `carrito.contieneItem()`. |

## Interfaces / Contracts

```java
// Producto.java — domain
boolean tieneStockSuficiente(int cantidad);
void descontarStock(int cantidad);  // throws StockInsuficienteException
void restaurarStock(int cantidad);  // solo suma, no valida límite

// Pedido.java — domain
void calcularTotal();  // itera detalles, settea total
boolean puedeCancelar();
void cancelar();       // throws ValidacionNegocioException
boolean puedePagar();
void pagar();          // throws ValidacionNegocioException

// Carrito.java — domain
boolean contieneItem(Long itemId);
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit (domain) | `Producto.tieneStockSuficiente`, `descontarStock`, `restaurarStock` | Tests directos en `ProductoTest.java` (nuevo). Escenarios: stock suficiente, insuficiente, sin efectos secundarios. |
| Unit (domain) | `Pedido.cancelar`, `pagar`, `puedeCancelar`, `puedePagar`, `calcularTotal` | Tests directos en `PedidoTest.java` (expandir existente). Escenarios por spec. |
| Unit (domain) | `Carrito.contieneItem` | Tests directos en `CarritoTest.java` (expandir). |
| Integration | Use cases delegando al dominio | Tests existentes de `PedidoUseCaseTest` y `CarritoUseCaseTest` con Mockito — deben pasar sin cambios de lógica de test. |
| Compilación | Firmas correctas | `./mvnw clean compile`. |
| Regresión | APIs sin cambios de comportamiento | `./mvnw test` completo. |

## Migration / Rollout

No migration required. Solo cambia implementación interna. APIs públicas (controladores, DTOs) no se modifican.

## Open Questions

- [ ] `restaurarStock()` debe validar contra un máximo o solo sumar? La spec no define límite superior — se implementa como suma pura.
- [ ] `ValidacionNegocioException` en el dominio: aceptamos el acoplamiento a `common/exception/` (mismo nivel que `BusinessException`) — se alinea con la decisión 1.
