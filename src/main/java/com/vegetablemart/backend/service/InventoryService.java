package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.inventory.AddStockRequest;
import com.vegetablemart.backend.dto.inventory.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse addStock(AddStockRequest request);

    InventoryResponse getInventoryByVegetable(
            Long vegetableId
    );

    List<InventoryResponse> getAllInventory();

    InventoryResponse getInventoryById(
            Long inventoryId
    );
}