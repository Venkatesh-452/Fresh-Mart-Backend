package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.vegetable.VegetableRequest;
import com.vegetablemart.backend.dto.vegetable.VegetableResponse;
import com.vegetablemart.backend.service.VegetableService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vegetables")
@RequiredArgsConstructor
@Validated
public class VegetableController {

    private final VegetableService vegetableService;

    // CREATE - ADMIN
    @PostMapping
    public ResponseEntity<VegetableResponse> createVegetable(
            @Valid @RequestBody VegetableRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vegetableService.createVegetable(request));
    }

    // GET ALL ACTIVE VEGETABLES - PUBLIC
    @GetMapping
    public ResponseEntity<List<VegetableResponse>> getAllVegetables() {
        return ResponseEntity.ok(vegetableService.getAllVegetables());
    }

    // GET ACTIVE VEGETABLE BY ID - PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<VegetableResponse> getVegetableById(
            @PathVariable @Positive(message = "Vegetable ID must be positive") Long id) {
        return ResponseEntity.ok(vegetableService.getVegetableById(id));
    }

    // GET ACTIVE VEGETABLES BY CATEGORY - PUBLIC
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<VegetableResponse>> getVegetablesByCategory(
            @PathVariable @Positive(message = "Category ID must be positive") Long categoryId) {
        return ResponseEntity.ok(vegetableService.getVegetablesByCategory(categoryId));
    }

    // UPDATE - ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<VegetableResponse> updateVegetable(
            @PathVariable @Positive(message = "Vegetable ID must be positive") Long id,
            @Valid @RequestBody VegetableRequest request) {
        return ResponseEntity.ok(vegetableService.updateVegetable(id, request));
    }

    // SOFT DELETE - ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVegetable(
            @PathVariable @Positive(message = "Vegetable ID must be positive") Long id) {
        vegetableService.deleteVegetable(id);
        return ResponseEntity.noContent().build();
    }
}
