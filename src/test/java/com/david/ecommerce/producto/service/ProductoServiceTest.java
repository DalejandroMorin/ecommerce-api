package com.david.ecommerce.producto.service;

import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.producto.dto.ProductoRequestDTO;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crearProducto_PrecioNegativo_LanzaExcepcion() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Teclado");
        dto.setPrecio(new BigDecimal("-10.00"));
        dto.setStock(5);

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("precio no puede ser negativo");
    }

    @Test
    void crearProducto_StockNegativo_LanzaExcepcion() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Monitor");
        dto.setPrecio(BigDecimal.TEN);
        dto.setStock(-3);

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("stock no puede ser negativo");
    }

    @Test
    void crearProducto_NombreVacio_LanzaExcepcion() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("");
        dto.setPrecio(BigDecimal.TEN);
        dto.setStock(1);

        assertThatThrownBy(() -> productoService.crear(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("nombre del producto es obligatorio");
    }
}