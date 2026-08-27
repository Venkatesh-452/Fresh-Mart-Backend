package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Vegetable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VegetableRepository
        extends JpaRepository<Vegetable, Long> {

    boolean existsByName(String name);

    Optional<Vegetable> findByName(String name);

    List<Vegetable> findByCategoryId(Long categoryId);

    List<Vegetable> findByActiveTrue();
}