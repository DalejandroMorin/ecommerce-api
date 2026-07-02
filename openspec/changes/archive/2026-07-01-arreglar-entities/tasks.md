# Tasks: Arreglar Entities JPA

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 80-120 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Delivery strategy | ask-on-risk |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: Low

### Suggested Work Units

N/A — single PR under 120 lines, no split needed.

## Phase 1: Entities sin relaciones (Producto + Usuario)

- [x] 1.1 `ProductoEntity.java` — `@Data` → `@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString`. `@EqualsAndHashCode.Include` en `nombre`. `@EqualsAndHashCode.Exclude` en `version`. `@Version private Long version;`. Eliminar `@AllArgsConstructor` + `import lombok.AllArgsConstructor`. Agregar imports: `lombok.Getter`, `lombok.Setter`, `lombok.ToString`, `lombok.EqualsAndHashCode`.
- [x] 1.2 `UsuarioEntity.java` — `@Data` → `@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true) @ToString`. `@EqualsAndHashCode.Include` en `email`. `@EqualsAndHashCode.Exclude` en `version`. `@Version`. Eliminar `@AllArgsConstructor`. Agregar `length = 255` en `@Column` de `password`. Mismos cambios de imports que 1.1.

## Phase 2: Entities con relaciones (Carrito + ItemCarrito)

- [x] 2.1 `CarritoEntity.java` — `@Data` → `@Getter @Setter @ToString @EqualsAndHashCode`. `@ToString.Exclude` en `usuario`, `items`. `@EqualsAndHashCode.Exclude` en `usuario`, `items`, `version`. `@Version`. Eliminar `@AllArgsConstructor` + import. Agregar imports: `lombok.ToString`, `lombok.EqualsAndHashCode`, `lombok.Getter`, `lombok.Setter`.
- [x] 2.2 `ItemCarritoEntity.java` — `@Data` → `@Getter @Setter @ToString @EqualsAndHashCode`. `@ToString.Exclude` en `carrito`, `producto`. `@EqualsAndHashCode.Exclude` en `carrito`, `producto`, `version`. `@Version`. Eliminar `@AllArgsConstructor`. Eliminar import redundante `com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity`. Mismos cambios de imports que 2.1.

## Phase 3: Entities con relaciones (Pedido + DetallePedido)

- [x] 3.1 `PedidoEntity.java` — `@Data` → `@Getter @Setter @ToString @EqualsAndHashCode`. `@ToString.Exclude` en `usuario`, `detalles`. `@EqualsAndHashCode.Exclude` en `usuario`, `detalles`, `version`. `@Version`. Eliminar `@AllArgsConstructor`. Mismos cambios de imports que 2.1.
- [x] 3.2 `DetallePedidoEntity.java` — `@Data` → `@Getter @Setter @ToString @EqualsAndHashCode`. `@ToString.Exclude` en `pedido`, `producto`. `@EqualsAndHashCode.Exclude` en `pedido`, `producto`, `version`. `@Version`. Eliminar `@AllArgsConstructor`. Eliminar import redundante `com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity`. Mismos cambios de imports.

## Phase 4: Verificación

- [x] 4.1 `./mvnw clean compile` — build sin errores de compilación
- [x] 4.2 `./mvnw test` — 124/125 tests pasan (solo falla `EcommerceApplicationTests.contextLoads`, pre-existing por falta de PostgreSQL)
