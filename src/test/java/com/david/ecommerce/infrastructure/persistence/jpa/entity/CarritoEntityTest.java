package com.david.ecommerce.infrastructure.persistence.jpa.entity;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.common.Rol;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CarritoEntityTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Cascade ALL persiste CarritoEntity con ItemCarritoEntity hijos")
    void cascadePersist_ConItems_GuardaTodoCorrectamente() {
        var usuario = crearUsuario();
        em.persistAndFlush(usuario);

        var producto = crearProducto();
        em.persistAndFlush(producto);
        em.clear();

        var usuarioManaged = em.find(UsuarioEntity.class, usuario.getId());
        var productoManaged = em.find(ProductoEntity.class, producto.getId());

        var item = new ItemCarritoEntity();
        item.setProducto(productoManaged);
        item.setCantidad(2);
        item.setPrecioUnitario(new BigDecimal("2500.00"));

        var carrito = new CarritoEntity();
        carrito.setUsuario(usuarioManaged);
        carrito.setItems(java.util.List.of(item));
        item.setCarrito(carrito);

        var saved = em.persistAndFlush(carrito);
        em.clear();

        var found = em.find(CarritoEntity.class, saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUsuario().getId()).isEqualTo(usuario.getId());
        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().getFirst().getProducto().getId()).isEqualTo(producto.getId());
        assertThat(found.getItems().getFirst().getCantidad()).isEqualTo(2);
        assertThat(found.getItems().getFirst().getPrecioUnitario())
                .isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("orphanRemoval elimina ItemCarritoEntity al removerlo de la colección")
    void orphanRemoval_RemoverItem_EliminaItemDeBD() {
        var usuario = crearUsuario();
        em.persistAndFlush(usuario);

        var producto = crearProducto();
        em.persistAndFlush(producto);
        em.clear();

        var usuarioManaged = em.find(UsuarioEntity.class, usuario.getId());
        var productoManaged = em.find(ProductoEntity.class, producto.getId());

        var item = new ItemCarritoEntity();
        item.setProducto(productoManaged);
        item.setCantidad(1);
        item.setPrecioUnitario(new BigDecimal("100.00"));

        var carrito = new CarritoEntity();
        carrito.setUsuario(usuarioManaged);
        carrito.setItems(new java.util.ArrayList<>(java.util.List.of(item)));
        item.setCarrito(carrito);

        var saved = em.persistAndFlush(carrito);
        em.clear();

        var found = em.find(CarritoEntity.class, saved.getId());
        assertThat(found.getItems()).hasSize(1);

        found.getItems().clear();
        em.persistAndFlush(found);
        em.clear();

        var afterRemoval = em.find(CarritoEntity.class, saved.getId());

        assertThat(afterRemoval).isNotNull();
        assertThat(afterRemoval.getItems()).isEmpty();
    }

    @Test
    @DisplayName("FetchType.LAZY en @OneToOne a Usuario no falla dentro de @DataJpaTest transaccional")
    void lazyLoad_Usuario_DentroDeTransaccion() {
        var usuario = crearUsuario();
        em.persistAndFlush(usuario);

        var carrito = new CarritoEntity();
        carrito.setUsuario(usuario);

        var saved = em.persistAndFlush(carrito);
        em.clear();

        var found = em.find(CarritoEntity.class, saved.getId());

        // el test está dentro de @Transactional, la session está abierta
        assertThat(found.getUsuario().getNombre()).isEqualTo("Carlos López");
    }

    private UsuarioEntity crearUsuario() {
        var usuario = new UsuarioEntity();
        usuario.setNombre("Carlos López");
        usuario.setEmail("carlos@ejemplo.com");
        usuario.setPassword("pass123");
        usuario.setRol(Rol.CLIENTE);
        return usuario;
    }

    private ProductoEntity crearProducto() {
        var producto = new ProductoEntity();
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(10);
        producto.setCategoria(Categoria.OTROS);
        return producto;
    }
}
