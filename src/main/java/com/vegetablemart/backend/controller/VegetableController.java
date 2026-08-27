package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.vegetable.VegetableRequest;
import com.vegetablemart.backend.dto.vegetable.VegetableResponse;
import com.vegetablemart.backend.service.VegetableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vegetables")
@RequiredArgsConstructor
public class VegetableController {

    private final VegetableService vegetableService;

    // CREATE
    @PostMapping
    public ResponseEntity<VegetableResponse> createVegetable(
            @Valid @RequestBody VegetableRequest request
    ) {
        VegetableResponse response =
                vegetableService.createVegetable(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<VegetableResponse>> getAllVegetables() {

        return ResponseEntity.ok(
                vegetableService.getAllVegetables()
        );
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<VegetableResponse> getVegetableById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                vegetableService.getVegetableById(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<VegetableResponse> updateVegetable(
            @PathVariable Long id,
            @Valid @RequestBody VegetableRequest request
    ) {

        return ResponseEntity.ok(
                vegetableService.updateVegetable(id, request)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVegetable(
            @PathVariable Long id
    ) {

        vegetableService.deleteVegetable(id);

        return ResponseEntity.noContent().build();
    }
}