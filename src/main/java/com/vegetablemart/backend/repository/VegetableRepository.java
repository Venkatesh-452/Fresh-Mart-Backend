package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Vegetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VegetableRepository extends JpaRepository<Vegetable, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    Optional<Vegetable> findByNameIgnoreCase(String name);

    Optional<Vegetable> findByIdAndActiveTrue(Long id);

    List<Vegetable> findByActiveTrue();

    List<Vegetable> findByCategoryIdAndActiveTrue(Long categoryId);
}
