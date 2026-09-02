package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.category.CategoryRequest;
import com.vegetablemart.backend.dto.category.CategoryResponse;
import com.vegetablemart.backend.entity.Category;
import com.vegetablemart.backend.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException(
                    "Category already exists with name: " + name
            );
        }

        String description = request.getDescription();

        Category category = Category.builder()
                .name(name)
                .description(
                        description != null
                                ? description.trim()
                                : null
                )
                .active(true)
                .build();

        Category savedCategory =
                categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    // =========================================================
    // GET ALL ACTIVE CATEGORIES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET CATEGORY BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {

        validateId(id);

        Category category =
                categoryRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: " + id
                                )
                        );

        return mapToResponse(category);
    }

    // =========================================================
    // UPDATE CATEGORY
    // =========================================================

    @Override
    public CategoryResponse updateCategory(
            Long id,
            CategoryRequest request
    ) {

        validateId(id);

        Category category =
                categoryRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: " + id
                                )
                        );

        String name = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCase(name)) {

            throw new RuntimeException(
                    "Category already exists with name: " + name
            );
        }

        category.setName(name);

        String description = request.getDescription();

        category.setDescription(
                description != null
                        ? description.trim()
                        : null
        );

        return mapToResponse(category);
    }

    // =========================================================
    // SOFT DELETE CATEGORY
    // =========================================================

    @Override
    public void deleteCategory(Long id) {

        validateId(id);

        Category category =
                categoryRepository.findByIdAndActiveTrue(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: " + id
                                )
                        );

        category.setActive(false);
    }

    // =========================================================
    // VALIDATE ID
    // =========================================================

    private void validateId(Long id) {

        if (id == null || id <= 0) {
            throw new RuntimeException(
                    "Invalid category ID"
            );
        }
    }

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    private CategoryResponse mapToResponse(
            Category category
    ) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}