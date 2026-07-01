package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaProductoRepositoryAdapter.class)
class JpaProductoRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private JpaProductoRepositoryAdapter adapter;

    @Autowired
    private TestEntityManager em;

    private ProductoEntity laptopEntity;
    private ProductoEntity mouseEntity;

    @BeforeEach
    void setUp() {
        laptopEntity = new ProductoEntity();
        laptopEntity.setNombre("Laptop Gamer");
        laptopEntity.setDescripcion("Laptop potente");
        laptopEntity.setPrecio(new BigDecimal("2500.00"));
        laptopEntity.setStock(10);
        laptopEntity.setCategoria(Categoria.ELECTRONICA);
        em.persistAndFlush(laptopEntity);

        mouseEntity = new ProductoEntity();
        mouseEntity.setNombre("Mouse Inalámbrico");
        mouseEntity.setDescripcion("Mouse ergonómico");
        mouseEntity.setPrecio(new BigDecimal("80.00"));
        mouseEntity.setStock(50);
        mouseEntity.setCategoria(Categoria.ELECTRONICA);
        em.persistAndFlush(mouseEntity);

        var libroEntity = new ProductoEntity();
        libroEntity.setNombre("Libro de Java");
        libroEntity.setDescripcion("Programación avanzada");
        libroEntity.setPrecio(new BigDecimal("45.00"));
        libroEntity.setStock(5);
        libroEntity.setCategoria(Categoria.LIBROS);
        em.persistAndFlush(libroEntity);

        em.clear();
    }

    @Test
    @DisplayName("findAll retorna todos los productos")
    void findAll_ConDatos_RetornaTodos() {
        var resultados = adapter.findAll();

        assertThat(resultados).hasSize(3);
    }

    @Test
    @DisplayName("findById con ID existente retorna Optional con producto")
    void findById_Existente_RetornaProducto() {
        var resultado = adapter.findById(laptopEntity.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Laptop Gamer");
    }

    @Test
    @DisplayName("findById con ID inexistente retorna Optional vacío")
    void findById_NoExistente_RetornaVacio() {
        var resultado = adapter.findById(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save crea un nuevo producto")
    void save_Crear_RetornaProductoCreado() {
        var nuevo = new Producto("Teclado Mecánico", "Teclado RGB",
                new BigDecimal("200.00"), 15, null, Categoria.ELECTRONICA);

        var resultado = adapter.save(nuevo);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Teclado Mecánico");

        var encontrado = adapter.findById(resultado.getId());
        assertThat(encontrado).isPresent();
    }

    @Test
    @DisplayName("save actualiza un producto existente")
    void save_Actualizar_RetornaProductoActualizado() {
        var productoDomain = adapter.findById(laptopEntity.getId()).orElseThrow();
        productoDomain.setPrecio(new BigDecimal("2300.00"));
        productoDomain.cambiarStock(8);

        var resultado = adapter.save(productoDomain);

        assertThat(resultado.getPrecio()).isEqualByComparingTo(new BigDecimal("2300.00"));
        assertThat(resultado.getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("deleteById elimina un producto")
    void deleteById_Existente_ProductoEliminado() {
        adapter.deleteById(laptopEntity.getId());

        var resultado = adapter.findById(laptopEntity.getId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsById retorna true para ID existente")
    void existsById_Existente_RetornaTrue() {
        assertThat(adapter.existsById(laptopEntity.getId())).isTrue();
    }

    @Test
    @DisplayName("existsById retorna false para ID inexistente")
    void existsById_NoExistente_RetornaFalse() {
        assertThat(adapter.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("buscarConFiltros filtra por nombre (LIKE)")
    void buscarConFiltros_PorNombre_RetornaCoincidencias() {
        var resultados = adapter.buscarConFiltros("Laptop", null, null, null, null);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).contains("Laptop");
    }

    @Test
    @DisplayName("buscarConFiltros filtra por categoria y rango de precio")
    void buscarConFiltros_PorCategoriaYPrecio_RetornaFiltrados() {
        var resultados = adapter.buscarConFiltros(
                null, Categoria.ELECTRONICA,
                new BigDecimal("50"), new BigDecimal("100"), null);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).isEqualTo("Mouse Inalámbrico");
    }

    @Test
    @DisplayName("buscarConFiltros con parámetros que no coinciden retorna lista vacía")
    void buscarConFiltros_SinCoincidencias_RetornaVacio() {
        var resultados = adapter.buscarConFiltros(null, null, null, null, 999);

        assertThat(resultados).isEmpty();
    }

    @Test
    @DisplayName("buscarConFiltros con todos los parámetros null retorna todos (EC-5)")
    void buscarConFiltros_TodosNull_RetornaTodos() {
        var resultados = adapter.buscarConFiltros(null, null, null, null, null);

        assertThat(resultados).hasSize(3);
    }
}
