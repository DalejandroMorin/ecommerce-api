# Proposal: arreglar-entities

## Intent

Las 6 JPA entities usan `@Data` de Lombok, lo que genera bugs y malas prácticas: `toString()` recursivo en relaciones bidireccionales que causa stack overflow, `equals()`/`hashCode()` con `id` transitorio que produce comportamiento inconsistente en `Set`/`merge`, ausencia de `@Version` que permite lost updates concurrentes, y `@AllArgsConstructor` que expone el riesgo de construir entidades con `id` preexistente.

## Scope

### In Scope

- Reemplazar `@Data` por `@Getter @Setter` en las 6 entities
- Agregar `@ToString.Exclude` en todas las relaciones LAZY (`@ManyToOne`, `@OneToOne`, `@OneToMany`)
- Agregar `@EqualsAndHashCode.Exclude` en relaciones LAZY y campo `version`
- Agregar `@Version private Long version;` en cada entity
- Eliminar `@AllArgsConstructor` de todas las entities
- Mantener `@NoArgsConstructor` requerido por JPA

### Out of Scope

- Value Objects (PR separado)
- Migraciones Flyway (diferido)
- Domain models o use cases (sin cambios)

## Capabilities

### New Capabilities

None — cambio puramente infraestructural sin nuevas capacidades funcionales.

### Modified Capabilities

None — no cambia contratos de spec existentes.

## Approach

1. En cada entity: `@Data` → `@Getter @Setter @ToString @EqualsAndHashCode`
2. Excluir relaciones LAZY con `@ToString.Exclude` y `@EqualsAndHashCode.Exclude`
3. Insertar `@Version private Long version;` y excluir de `equals`/`hashCode`
4. Remover `@AllArgsConstructor`; dejar solo `@NoArgsConstructor` y constructor(es) sin `id`
5. Verificar que adapters/mappers/usecases que construyan entities sigan compilando

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `ProductoEntity.java` | Modified | Correcciones Lombok + @Version |
| `UsuarioEntity.java` | Modified | Correcciones Lombok + @Version |
| `CarritoEntity.java` | Modified | + @ToString.Exclude en usuario/items + @Version |
| `ItemCarritoEntity.java` | Modified | + @ToString.Exclude en carrito/producto + @Version |
| `PedidoEntity.java` | Modified | + @ToString.Exclude en usuario/detalles + @Version |
| `DetallePedidoEntity.java` | Modified | + @ToString.Exclude en pedido/producto + @Version |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Adapters/mappers construyen entities pasando `id` en constructor | Low | Revisar usos de `new XxxEntity(...)` con args; `./mvnw compile` detecta errores |
| `@Version` causa `OptimisticLockException` inesperada en tests concurrentes | Med | Tests existentes no usan concurrencia; riesgo bajo para CI actual |
| Adapters existentes no setean `version` al crear entities | Low | `version` es `null` antes de persist — JPA lo maneja automáticamente |

## Rollback Plan

Revertir commit del cambio. No hay migraciones ni cambios de schema involucrados.

## Dependencies

Ninguna.

## Success Criteria

- [ ] `./mvnw clean compile` pasa sin errores
- [ ] `./mvnw test` pasa sin regresiones
- [ ] Cada entity tiene `@Getter @Setter` en lugar de `@Data`
- [ ] Cada entity tiene `@Version` field
- [ ] Ninguna entity tiene `@AllArgsConstructor`
- [ ] Todas las relaciones LAZY tienen `@ToString.Exclude`
- [ ] Stack trace de `toString()` en relación bidireccional ya no causa error
