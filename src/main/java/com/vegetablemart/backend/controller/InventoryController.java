package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.inventory.AddStockRequest;
import com.vegetablemart.backend.dto.inventory.InventoryResponse;
import com.vegetablemart.backend.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // =========================
    // ADMIN APIs
    // =========================

    @PostMapping("/add-stock")
    public ResponseEntity<InventoryResponse> addStock(
            @Valid @RequestBody AddStockRequest request
    ) {

        return ResponseEntity.ok(
                inventoryService.addStock(request)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {

        return ResponseEntity.ok(
                inventoryService.getAllInventory()
        );
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> getInventoryById(
            @PathVariable Long inventoryId
    ) {

        return ResponseEntity.ok(
                inventoryService.getInventoryById(
                        inventoryId
                )
        );
    }

    @GetMapping("/vegetable/{vegetableId}")
    public ResponseEntity<InventoryResponse> getInventoryByVegetable(
            @PathVariable Long vegetableId
    ) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByVegetable(
                        vegetableId
                )
        );
    }
}