package com.david.ecommerce.infrastructure.persistence.jpa.entity;

import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductoEntityTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("persistir producto con todos los campos y leerlo por ID")
    void persistir_ProductoConTodosLosCampos_RetornaProductoCreado() {
        var entity = new ProductoEntity();
        entity.setNombre("Laptop Gamer");
        entity.setDescripcion("Laptop para juegos con RTX 4090");
        entity.setPrecio(new BigDecimal("2500.00"));
        entity.setStock(10);
        entity.setImagenUrl("https://ejemplo.com/laptop.jpg");
        entity.setCategoria(ProductoEntity.Categoria.ELECTRONICA);

        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(ProductoEntity.class, saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getNombre()).isEqualTo("Laptop Gamer");
        assertThat(found.getDescripcion()).isEqualTo("Laptop para juegos con RTX 4090");
        assertThat(found.getPrecio()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(found.getStock()).isEqualTo(10);
        assertThat(found.getImagenUrl()).isEqualTo("https://ejemplo.com/laptop.jpg");
        assertThat(found.getCategoria()).isEqualTo(ProductoEntity.Categoria.ELECTRONICA);
    }

    @Test
    @DisplayName("@PrePersist establece fechaCreacion y fechaActualizacion al insertar")
    void prePersist_EstableceFechas_Correctamente() {
        var entity = new ProductoEntity();
        entity.setNombre("Teclado");
        entity.setPrecio(new BigDecimal("150.00"));
        entity.setStock(20);
        entity.setCategoria(ProductoEntity.Categoria.ELECTRONICA);

        var now = LocalDateTime.now();
        var beforePersist = now.withNano((now.getNano() / 1000) * 1000);
        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(ProductoEntity.class, saved.getId());

        assertThat(found.getFechaCreacion()).isNotNull();
        assertThat(found.getFechaCreacion()).isAfterOrEqualTo(beforePersist);
        assertThat(found.getFechaCreacion()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(found.getFechaActualizacion()).isNotNull();
        assertThat(found.getFechaActualizacion()).isAfterOrEqualTo(beforePersist);
    }

    @Test
    @DisplayName("@PreUpdate actualiza fechaActualizacion al modificar el producto")
    void preUpdate_ActualizaFechaActualizacion_Correctamente() {
        var entity = new ProductoEntity();
        entity.setNombre("Mouse");
        entity.setPrecio(new BigDecimal("80.00"));
        entity.setStock(30);
        entity.setCategoria(ProductoEntity.Categoria.ELECTRONICA);

        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(ProductoEntity.class, saved.getId());
        var fechaActualizacionOriginal = found.getFechaActualizacion();

        found.setStock(25);
        em.persistAndFlush(found);
        em.clear();

        var updated = em.find(ProductoEntity.class, saved.getId());

        assertThat(updated.getFechaActualizacion()).isAfter(fechaActualizacionOriginal);
    }

    @Test
    @DisplayName("Categoria enum se persiste como STRING y se lee correctamente para todos los valores")
    void categoriaEnum_SePersisteComoString_TodosLosValores() {
        for (ProductoEntity.Categoria categoria : ProductoEntity.Categoria.values()) {
            var entity = new ProductoEntity();
            entity.setNombre("Producto " + categoria.name());
            entity.setPrecio(new BigDecimal("10.00"));
            entity.setStock(5);
            entity.setCategoria(categoria);

            var saved = em.persistAndFlush(entity);
            em.clear();

            var found = em.find(ProductoEntity.class, saved.getId());

            assertThat(found.getCategoria()).isEqualTo(categoria);
        }
    }

    @Test
    @DisplayName("Verificar que todas las Categoria del dominio existen en la entidad (EC-1)")
    void enumSync_DominioAEntidad_TodasLasCategoriasMapean() {
        for (com.david.ecommerce.domain.producto.Producto.Categoria domainCat
                : com.david.ecommerce.domain.producto.Producto.Categoria.values()) {
            var entityCat = ProductoEntity.Categoria.valueOf(domainCat.name());
            assertThat(entityCat.name()).isEqualTo(domainCat.name());
        }
    }

    @Test
    @DisplayName("Columnas con restricciones de longitud y precisión se persisten correctamente")
    void columnas_ConRestricciones_SePersistenCorrectamente() {
        var entity = new ProductoEntity();
        entity.setNombre("A".repeat(150));
        entity.setDescripcion("B".repeat(1000));
        entity.setPrecio(new BigDecimal("99999999.99"));
        entity.setStock(Integer.MAX_VALUE);
        entity.setImagenUrl("C".repeat(500));
        entity.setCategoria(ProductoEntity.Categoria.OTROS);

        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(ProductoEntity.class, saved.getId());

        assertThat(found.getNombre()).hasSize(150);
        assertThat(found.getDescripcion()).hasSize(1000);
        assertThat(found.getPrecio()).isEqualByComparingTo(new BigDecimal("99999999.99"));
        assertThat(found.getStock()).isEqualTo(Integer.MAX_VALUE);
        assertThat(found.getImagenUrl()).hasSize(500);
    }
}
