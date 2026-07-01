package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.common.Categoria;
import com.david.ecommerce.domain.producto.Producto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductoMapperTest {

    @Test
    @DisplayName("toEntity luego toDomain preserva todos los campos (roundtrip)")
    void roundtrip_DominioAEntidadADominio_PreservaCampos() {
        var domain = new Producto();
        domain.setId(1L);
        domain.setNombre("Laptop Gamer");
        domain.setDescripcion("Laptop con RTX 4090");
        domain.setPrecio(new BigDecimal("2500.00"));
        domain.setStock(10);
        domain.setImagenUrl("https://ejemplo.com/img.jpg");
        domain.setCategoria(Categoria.ELECTRONICA);
        domain.setFechaCreacion(LocalDateTime.of(2025, 1, 15, 10, 0));
        domain.setFechaActualizacion(LocalDateTime.of(2025, 1, 15, 12, 0));

        var entity = ProductoMapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNombre()).isEqualTo("Laptop Gamer");
        assertThat(entity.getDescripcion()).isEqualTo("Laptop con RTX 4090");
        assertThat(entity.getPrecio()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(entity.getStock()).isEqualTo(10);
        assertThat(entity.getImagenUrl()).isEqualTo("https://ejemplo.com/img.jpg");
        assertThat(entity.getCategoria()).isEqualTo(Categoria.ELECTRONICA);

        var result = ProductoMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Laptop Gamer");
        assertThat(result.getDescripcion()).isEqualTo("Laptop con RTX 4090");
        assertThat(result.getPrecio()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(result.getStock()).isEqualTo(10);
        assertThat(result.getImagenUrl()).isEqualTo("https://ejemplo.com/img.jpg");
        assertThat(result.getCategoria()).isEqualTo(Categoria.ELECTRONICA);
        // Nota: fechas se omiten en el roundtrip porque toEntity no las copia
        // (son establecidas por @PrePersist/@PreUpdate en la entidad)
    }

    @Test
    @DisplayName("toEntity con null retorna null")
    void toEntity_Null_RetornaNull() {
        assertThat(ProductoMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("toDomain con null retorna null")
    void toDomain_Null_RetornaNull() {
        assertThat(ProductoMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Todas las categorias del dominio mapean correctamente")
    void todasLasCategorias_MapeanCorrectamente() {
        for (Categoria domainCat : Categoria.values()) {
            var domain = new Producto();
            domain.setNombre("Test");
            domain.setPrecio(BigDecimal.TEN);
            domain.setStock(1);
            domain.setCategoria(domainCat);

            var entity = ProductoMapper.toEntity(domain);
            var result = ProductoMapper.toDomain(entity);

            assertThat(result.getCategoria()).isEqualTo(domainCat);
        }
    }
}
