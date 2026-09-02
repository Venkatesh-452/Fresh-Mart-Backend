package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.category.CategoryRequest;
import com.vegetablemart.backend.dto.category.CategoryResponse;
import com.vegetablemart.backend.entity.Category;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.DuplicateResourceException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
import com.vegetablemart.backend.repository.CategoryRepository;
import com.vegetablemart.backend.repository.VegetableRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final VegetableRepository vegetableRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        validateRequest(request);
        String name = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category already exists with name: " + name);
        }

        Category category = Category.builder()
                .name(name)
                .description(trimDescription(request.getDescription()))
                .active(true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        validateId(id);
        return mapToResponse(findActiveCategory(id));
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        validateId(id);
        validateRequest(request);

        Category category = findActiveCategory(id);
        String name = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Category already exists with name: " + name);
        }

        category.setName(name);
        category.setDescription(trimDescription(request.getDescription()));
        return mapToResponse(category);
    }

    @Override
    public void deleteCategory(Long id) {
        validateId(id);
        Category category = findActiveCategory(id);

        if (vegetableRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new BadRequestException(
                    "Cannot delete category while active vegetables are assigned to it"
            );
        }

        category.setActive(false);
    }

    private Category findActiveCategory(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    private void validateRequest(CategoryRequest request) {
        if (request == null) {
            throw new BadRequestException("Category request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Category name is required");
        }
        if (request.getName().trim().length() > 100) {
            throw new BadRequestException("Category name cannot exceed 100 characters");
        }
        if (request.getDescription() != null && request.getDescription().trim().length() > 500) {
            throw new BadRequestException("Category description cannot exceed 500 characters");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("Invalid category ID");
        }
    }

    private String trimDescription(String description) {
        if (description == null) return null;
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private CategoryResponse mapToResponse(Category category) {
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
