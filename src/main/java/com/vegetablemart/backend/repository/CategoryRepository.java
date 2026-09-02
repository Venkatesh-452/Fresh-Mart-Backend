package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByIdAndActiveTrue(Long id);

    List<Category> findByActiveTrue();
}