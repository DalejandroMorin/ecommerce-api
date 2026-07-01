package com.david.ecommerce.domain.producto;

import com.david.ecommerce.domain.common.Categoria;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> findAll();
    Optional<Producto> findById(Long id);
    Producto save(Producto producto);
    void deleteById(Long id);
    boolean existsById(Long id);
    List<Producto> buscarConFiltros(String nombre, Categoria categoria,
                                    BigDecimal precioMin, BigDecimal precioMax, Integer stockMin);
}
