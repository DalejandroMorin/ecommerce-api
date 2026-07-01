package com.david.ecommerce.infrastructure.persistence.jpa.repository;

import com.david.ecommerce.infrastructure.persistence.jpa.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {

    List<PedidoEntity> findByUsuarioId(Long usuarioId);
}
