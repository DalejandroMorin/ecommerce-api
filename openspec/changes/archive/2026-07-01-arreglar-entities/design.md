# Design: Arreglar Entidades JPA

## Technical Approach

Refactor localizado en las 6 JPA entities (`infrastructure/persistence/jpa/entity/`) para eliminar bugs de `@Data`, agregar optimistic locking con `@Version`, estabilizar equals/hashCode con business keys, y remover `@AllArgsConstructor`. Sin cambios en schema, domain models, use cases, controllers, ni tests funcionales.

## Architecture Decisions

### Decision: equals/hashCode — business key por entity

| Entity | Business key | Lombok field | Relaciones excluidas |
|--------|-------------|-------------|---------------------|
| `ProductoEntity` | `nombre` | `@EqualsAndHashCode.Include` en `nombre` | `version` excluido |
| `UsuarioEntity` | `email` | `@EqualsAndHashCode.Include` en `email` | `version` excluido |
| `CarritoEntity` | `id` (1:1 con usuario) | `@EqualsAndHashCode.Include` en `id` | `usuario`, `items`, `version` |
| `ItemCarritoEntity` | `id` | default (sin Include) | `carrito`, `producto`, `version` |
| `PedidoEntity` | `id` | default (sin Include) | `usuario`, `detalles`, `version` |
| `DetallePedidoEntity` | `id` | default (sin Include) | `pedido`, `producto`, `version` |

Las entities sin business key natural (`id`-based) no se usan en Sets en la aplicación actual — `id` es aceptable.

### Decision: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en ProductoEntity y UsuarioEntity

**Choice**: Activar `onlyExplicitlyIncluded` en las entities con business key real.
**Alternatives**: Usar default de Lombok que incluye todos los campos.
**Rationale**: Protege contra inclusiones accidentales de nuevos campos o relaciones en equals/hashCode.

### Decision: `@ToString.Exclude` en todas las relaciones LAZY

**Choice**: Marcar todo `@ManyToOne`, `@OneToMany`, `@OneToOne` con `@ToString.Exclude`.
**Rationale**: Previene stack overflow en `toString()` bidireccional y `LazyInitializationException` al serializar.

### Decision: `@Version` en las 6 entities

**Choice**: `@Version private Long version;` en todas, incluso las sin relaciones.
**Rationale**: Consistencia. Protección contra lost updates concurrentes. Cero costo de mantenimiento.

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `entity/ProductoEntity.java` | Modify | `@Data`→`@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded=true)`, +`@EqualsAndHashCode.Include` en nombre, +`@Version`, -`@AllArgsConstructor` |
| `entity/UsuarioEntity.java` | Modify | `@Data`→`@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded=true)`, +`@EqualsAndHashCode.Include` en email, +`@Version`, -`@AllArgsConstructor` |
| `entity/CarritoEntity.java` | Modify | `@Data`→`@Getter @Setter @ToString @EqualsAndHashCode`, +`@ToString.Exclude` en usuario/items, +`@EqualsAndHashCode.Exclude` en usuario/items/version, +`@Version`, -`@AllArgsConstructor` |
| `entity/ItemCarritoEntity.java` | Modify | `@Data`→`@Getter @Setter @ToString @EqualsAndHashCode`, +`@ToString.Exclude` en carrito/producto, +`@EqualsAndHashCode.Exclude` en carrito/producto/version, +`@Version`, -`@AllArgsConstructor`, limpiar import redundante `ProductoEntity` |
| `entity/PedidoEntity.java` | Modify | `@Data`→`@Getter @Setter @ToString @EqualsAndHashCode`, +`@ToString.Exclude` en usuario/detalles, +`@EqualsAndHashCode.Exclude` en usuario/detalles/version, +`@Version`, -`@AllArgsConstructor` |
| `entity/DetallePedidoEntity.java` | Modify | `@Data`→`@Getter @Setter @ToString @EqualsAndHashCode`, +`@ToString.Exclude` en pedido/producto, +`@EqualsAndHashCode.Exclude` en pedido/producto/version, +`@Version`, -`@AllArgsConstructor`, limpiar import redundante `ProductoEntity` |

## Interfaces / Contracts

Sin cambios en APIs. Los mappers (`ProductoMapper`, `UsuarioMapper`, etc.) usan setters — al reemplazar `@Data` por `@Getter @Setter`, los setters generados son idénticos. Todos los constructores sin argumentos existentes (`new ProductoEntity()`, etc.) siguen funcionando.

## Testing Strategy

| Capa | Qué testear | Approach |
|------|-------------|----------|
| Compilación | Anotaciones correctas | `./mvnw clean compile` |
| Regresión | Comportamiento no alterado | `./mvnw test` — suite completa |
| Entidades | equals/hashCode estables | Tests existentes en `entity/*Test.java` (5 tests) |

## Migration / Rollout

No se requiere migración de datos. `@Version` se agrega como columna nullable — Hibernate asigna `0` en primera persistencia automáticamente. Rollback: revertir commit. Sin side effects.

## Open Questions

None.
