package com.david.ecommerce.infrastructure.persistence.jpa.repository;

import com.david.ecommerce.infrastructure.persistence.jpa.entity.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    List<ProductoEntity> findByNombreContainingIgnoreCase(String nombre);

    List<ProductoEntity> findByCategoria(ProductoEntity.Categoria categoria);

    List<ProductoEntity> findByPrecioBetween(BigDecimal precioMin, BigDecimal precioMax);

    List<ProductoEntity> findByNombreContainingIgnoreCaseAndCategoria(String nombre, ProductoEntity.Categoria categoria);

    @Query("SELECT p FROM ProductoEntity p WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:categoria IS NULL OR p.categoria = :categoria) AND " +
            "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
            "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
            "(:stockMin IS NULL OR p.stock >= :stockMin)")
    List<ProductoEntity> buscarConFiltros(@Param("nombre") String nombre,
                                          @Param("categoria") ProductoEntity.Categoria categoria,
                                          @Param("precioMin") BigDecimal precioMin,
                                          @Param("precioMax") BigDecimal precioMax,
                                          @Param("stockMin") Integer stockMin);
}
