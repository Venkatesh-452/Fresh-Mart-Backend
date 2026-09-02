package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.category.CategoryRequest;
import com.vegetablemart.backend.dto.category.CategoryResponse;
import com.vegetablemart.backend.service.CategoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    // =========================================================
    // PUBLIC APIs
    // =========================================================

    // GET ALL ACTIVE CATEGORIES
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // GET ACTIVE CATEGORY BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable @Positive(message = "Category ID must be positive") Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // =========================================================
    // ADMIN APIs
    // =========================================================

    // CREATE CATEGORY
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    // UPDATE CATEGORY
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable @Positive(message = "Category ID must be positive") Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // SOFT DELETE CATEGORY
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable @Positive(message = "Category ID must be positive") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
