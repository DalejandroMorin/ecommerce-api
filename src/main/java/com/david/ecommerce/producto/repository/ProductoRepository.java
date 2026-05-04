package com.david.ecommerce.producto.repository;

import com.david.ecommerce.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Los métodos básicos los hereda de JpaRepository:
    // findAll(), findById(), save(), deleteById()

    // Búsqueda por nombre (contiene, case insensitive)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Búsqueda por categoría
    List<Producto> findByCategoria(Producto.Categoria categoria);

    // Búsqueda por rango de precio
    List<Producto> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    // Búsqueda por nombre Y categoría
    List<Producto> findByNombreContainingIgnoreCaseAndCategoria(String nombre, Producto.Categoria categoria);

    // JPQL para búsqueda avanzada con múltiples filtros opcionales
    @Query("SELECT p FROM Producto p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:stockMin IS NULL OR p.stock >= :stockMin)")
    List<Producto> buscarConFiltros(@Param("nombre") String nombre,
                                    @Param("categoria") Producto.Categoria categoria,
                                    @Param("precioMin") BigDecimal precioMin,
                                    @Param("precioMax") BigDecimal precioMax,
                                    @Param("stockMin") Integer stockMin);
}