package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.inventory.AddStockRequest;
import com.vegetablemart.backend.dto.inventory.InventoryResponse;
import com.vegetablemart.backend.entity.Inventory;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
import com.vegetablemart.backend.repository.InventoryRepository;
import com.vegetablemart.backend.repository.VegetableRepository;
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
    public InventoryResponse addStock(AddStockRequest request) {
        if (request == null || request.getVegetableId() == null || request.getVegetableId() <= 0)
            throw new BadRequestException("Valid vegetable ID is required");
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Stock quantity must be greater than 0");

        Vegetable vegetable = vegetableRepository.findById(request.getVegetableId())
                .orElseThrow(() -> new ResourceNotFoundException("Vegetable not found with ID: " + request.getVegetableId()));
        if (!Boolean.TRUE.equals(vegetable.getActive()))
            throw new BadRequestException("Cannot add stock to an inactive vegetable");

        Inventory inventory = inventoryRepository.findByVegetableId(vegetable.getId())
                .orElseGet(() -> Inventory.builder().vegetable(vegetable).totalStock(BigDecimal.ZERO)
                        .soldQuantity(BigDecimal.ZERO).availableQuantity(BigDecimal.ZERO).build());

        BigDecimal totalStock = inventory.getTotalStock() == null ? BigDecimal.ZERO : inventory.getTotalStock();
        BigDecimal availableQuantity = inventory.getAvailableQuantity() == null ? BigDecimal.ZERO : inventory.getAvailableQuantity();
        BigDecimal soldQuantity = inventory.getSoldQuantity() == null ? BigDecimal.ZERO : inventory.getSoldQuantity();
        BigDecimal quantity = request.getQuantity();

        inventory.setTotalStock(totalStock.add(quantity));
        inventory.setAvailableQuantity(availableQuantity.add(quantity));
        inventory.setSoldQuantity(soldQuantity);
        vegetable.setQuantity(inventory.getAvailableQuantity());
        vegetableRepository.save(vegetable);
        return mapToResponse(inventoryRepository.save(inventory));
    }

    @Override @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVegetable(Long vegetableId) {
        validateId(vegetableId);
        return mapToResponse(inventoryRepository.findByVegetableId(vegetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for vegetable ID: " + vegetableId)));
    }

    @Override @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long inventoryId) {
        validateId(inventoryId);
        return mapToResponse(inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + inventoryId)));
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) throw new BadRequestException("ID must be positive");
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        Vegetable vegetable = inventory.getVegetable();
        return InventoryResponse.builder().inventoryId(inventory.getId()).vegetableId(vegetable.getId())
                .vegetableName(vegetable.getName()).imageUrl(vegetable.getImageUrl())
                .totalStock(inventory.getTotalStock()).soldQuantity(inventory.getSoldQuantity())
                .availableQuantity(inventory.getAvailableQuantity()).unit(vegetable.getUnit())
                .lastUpdated(inventory.getLastUpdated()).build();
    }
}