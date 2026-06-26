package com.david.ecommerce.infrastructure.persistence.jpa.repository;

import com.david.ecommerce.infrastructure.persistence.jpa.entity.ItemCarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCarritoJpaRepository extends JpaRepository<ItemCarritoEntity, Long> {
}
