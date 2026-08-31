package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.inventory.AddStockRequest;
import com.vegetablemart.backend.dto.inventory.InventoryResponse;
import com.vegetablemart.backend.entity.Inventory;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.repository.InventoryRepository;
import com.vegetablemart.backend.repository.VegetableRepository;
import com.vegetablemart.backend.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final VegetableRepository vegetableRepository;

    @Override
    public InventoryResponse addStock(
            AddStockRequest request
    ) {

        // 1. Find vegetable
        Vegetable vegetable = vegetableRepository
                .findById(request.getVegetableId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Vegetable not found with ID: "
                                        + request.getVegetableId()
                        )
                );

        // 2. Check if inventory already exists
        Inventory inventory = inventoryRepository
                .findByVegetableId(vegetable.getId())
                .orElse(null);

        if (inventory == null) {

            // First stock entry
            inventory = Inventory.builder()
                    .vegetable(vegetable)
                    .totalStock(request.getQuantity())
                    .soldQuantity(BigDecimal.ZERO)
                    .availableQuantity(request.getQuantity())
                    .build();

        } else {

            // Existing inventory
            BigDecimal newTotalStock =
                    inventory.getTotalStock()
                            .add(request.getQuantity());

            BigDecimal newAvailableStock =
                    inventory.getAvailableQuantity()
                            .add(request.getQuantity());

            inventory.setTotalStock(newTotalStock);
            inventory.setAvailableQuantity(
                    newAvailableStock
            );
        }

        // 3. Keep Vegetable quantity synchronized
        vegetable.setQuantity(
                inventory.getAvailableQuantity()
        );

        vegetableRepository.save(vegetable);

        // 4. Save inventory
        inventory = inventoryRepository.save(inventory);

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVegetable(
            Long vegetableId
    ) {

        Inventory inventory = inventoryRepository
                .findByVegetableId(vegetableId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found for vegetable ID: "
                                        + vegetableId
                        )
                );

        return mapToResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {

        return inventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(
            Long inventoryId
    ) {

        Inventory inventory = inventoryRepository
                .findById(inventoryId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found with ID: "
                                        + inventoryId
                        )
                );

        return mapToResponse(inventory);
    }

    private InventoryResponse mapToResponse(
            Inventory inventory
    ) {

        Vegetable vegetable = inventory.getVegetable();

        return InventoryResponse.builder()
                .inventoryId(inventory.getId())
                .vegetableId(vegetable.getId())
                .vegetableName(vegetable.getName())
                .imageUrl(vegetable.getImageUrl())
                .totalStock(inventory.getTotalStock())
                .soldQuantity(inventory.getSoldQuantity())
                .availableQuantity(
                        inventory.getAvailableQuantity()
                )
                .unit(vegetable.getUnit())
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }
}