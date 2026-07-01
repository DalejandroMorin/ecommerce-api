# Proposal: Consolidar Enums Duplicados

## Intent

Eliminar la duplicación de 3 enums (`Categoria`, `Rol`, `EstadoPedido`) que existen tanto en las clases domain como en las JPA entities. Hoy los mappers traducen con `valueOf(name())` — si un enum diverge del otro, explota en runtime. Este refactor unifica cada concepto en un único archivo compartido en `domain/common/`, eliminando 6 definiciones inline y ~25 imports rotos.

## Scope

### In Scope
- Crear `domain/common/Categoria.java`, `domain/common/Rol.java`, `domain/common/EstadoPedido.java` con los mismos valores actuales
- Eliminar enums inline de `Producto.java`, `Usuario.java`, `Pedido.java`
- Eliminar enums inline de `ProductoEntity.java`, `UsuarioEntity.java`, `PedidoEntity.java`
- Actualizar imports en ~36 archivos (entities, mappers, adapters, repos, use cases, DTOs, controllers, security, tests)
- Eliminar traducciones `valueOf(name())` en mappers y adapters (ya no necesarias)
- Actualizar tests que referencian enums por su ruta anterior

### Out of Scope
- Value Objects (Email, Password, Money, etc.)
- Eliminar `setTotal` de Pedido
- Mover lógica de negocio (stock, transiciones) al dominio
- Cambios en comportamiento, lógica, o estructura de BBDD

## Capabilities

### New Capabilities
None

### Modified Capabilities
None — refactor puro, sin cambios en requirements a nivel spec.

## Approach

1. Crear 3 archivos enum en `domain/common/` copiando valores exactos de las definiciones actuales
2. Eliminar las 6 definiciones inline de domain/entity classes
3. Reemplazar imports `Producto.Categoria` → `Categoria`, `UsuarioEntity.Rol` → `Rol`, etc.
4. Simplificar mappers: `valueOf(name())` pasa a ser assignment directa (mismo enum class)
5. Compilar (`./mvnw clean compile`) para detectar imports rotos
6. Ejecutar tests (`./mvnw test`) para verificar

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `domain/common/` | New (3 files) | Enums compartidos |
| `domain/producto/Producto.java` | Modified | Eliminar enum inline Categoria |
| `domain/usuario/Usuario.java` | Modified | Eliminar enum inline Rol |
| `domain/pedido/Pedido.java` | Modified | Eliminar enum inline EstadoPedido |
| `infrastructure/.../entity/*Entity.java` | Modified (3) | Eliminar enums inline |
| `infrastructure/.../mapper/*Mapper.java` | Modified (3) | Simplificar traducciones |
| `infrastructure/.../adapter/` | Modified (2) | Actualizar imports |
| `application/`, `infrastructure/rest/`, `infrastructure/security/` | Modified (~15) | Actualizar imports |
| `src/test/java/` | Modified (~10) | Actualizar imports en tests |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Import olvidado rompe compilación | Medium | `mvnw clean compile` detecta todo |
| Tests fallan por import desactualizado | Low | `mvnw test` post-refactor lo cubre |
| Enum value diverge entre copias | High | Este PR justamente lo resuelve |

## Rollback Plan

`git revert` del commit de consolidación. Las 6 definiciones inline vuelven a su estado original y todo compila como antes.

## Dependencies

Ninguna.

## Success Criteria

- [ ] `./mvnw clean compile` pasa sin errores
- [ ] `./mvnw test` pasa sin errores
- [ ] No existen más definiciones duplicadas de `Categoria`, `Rol`, `EstadoPedido`
