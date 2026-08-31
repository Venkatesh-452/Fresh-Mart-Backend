package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVegetableId(Long vegetableId);

    boolean existsByVegetableId(Long vegetableId);
}