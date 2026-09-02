package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByVegetableId(Long vegetableId);

    boolean existsByVegetableId(Long vegetableId);
}
