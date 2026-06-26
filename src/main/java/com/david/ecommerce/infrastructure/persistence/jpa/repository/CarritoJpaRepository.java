package com.david.ecommerce.infrastructure.persistence.jpa.repository;

import com.david.ecommerce.infrastructure.persistence.jpa.entity.CarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoJpaRepository extends JpaRepository<CarritoEntity, Long> {

    Optional<CarritoEntity> findByUsuarioId(Long usuarioId);
}
