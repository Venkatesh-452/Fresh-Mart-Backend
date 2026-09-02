package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Vegetable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface VegetableRepository extends JpaRepository<Vegetable, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    Optional<Vegetable> findByNameIgnoreCase(String name);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vegetable> findById(Long id);

    Optional<Vegetable> findByIdAndActiveTrue(Long id);

    List<Vegetable> findByActiveTrue();

    List<Vegetable> findByCategoryIdAndActiveTrue(Long categoryId);
}
