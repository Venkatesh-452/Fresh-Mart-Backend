package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.inventory.AddStockRequest;
import com.vegetablemart.backend.dto.inventory.InventoryResponse;
import com.vegetablemart.backend.entity.Inventory;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.repository.InventoryRepository;
import com.vegetablemart.backend.repository.VegetableRepository;

import jakarta.validation.Valid;
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
    public InventoryResponse addStock(@Valid AddStockRequest request) {
        if (request == null || request.getVegetableId() == null) {
            throw new RuntimeException("Vegetable ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Stock quantity must be greater than 0");
        }

        Vegetable vegetable = vegetableRepository.findById(request.getVegetableId())
                .orElseThrow(() -> new RuntimeException(
                        "Vegetable not found with ID: " + request.getVegetableId()));

        if (!Boolean.TRUE.equals(vegetable.getActive())) {
            throw new RuntimeException("Cannot add stock to an inactive vegetable");
        }

        Inventory inventory = inventoryRepository.findByVegetableId(vegetable.getId())
                .orElseGet(() -> Inventory.builder()
                        .vegetable(vegetable)
                        .totalStock(BigDecimal.ZERO)
                        .soldQuantity(BigDecimal.ZERO)
                        .availableQuantity(BigDecimal.ZERO)
                        .build());

        BigDecimal quantity = request.getQuantity();
        inventory.setTotalStock(inventory.getTotalStock().add(quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(quantity));

        vegetable.setQuantity(inventory.getAvailableQuantity());
        vegetableRepository.save(vegetable);

        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVegetable(Long vegetableId) {
        validateId(vegetableId);

        Inventory inventory = inventoryRepository.findByVegetableId(vegetableId)
                .orElseThrow(() -> new RuntimeException(
                        "Inventory not found for vegetable ID: " + vegetableId));

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
    public InventoryResponse getInventoryById(Long inventoryId) {
        validateId(inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException(
                        "Inventory not found with ID: " + inventoryId));

        return mapToResponse(inventory);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("ID must be positive");
        }
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        Vegetable vegetable = inventory.getVegetable();

        return InventoryResponse.builder()
                .inventoryId(inventory.getId())
                .vegetableId(vegetable.getId())
                .vegetableName(vegetable.getName())
                .imageUrl(vegetable.getImageUrl())
                .totalStock(inventory.getTotalStock())
                .soldQuantity(inventory.getSoldQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .unit(vegetable.getUnit())
                .lastUpdated(inventory.getLastUpdated())
                .build();
    }
}
