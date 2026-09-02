package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.vegetable.VegetableRequest;
import com.vegetablemart.backend.dto.vegetable.VegetableResponse;
import com.vegetablemart.backend.entity.Category;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.repository.CategoryRepository;
import com.vegetablemart.backend.repository.VegetableRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VegetableServiceImpl implements VegetableService {

    private final VegetableRepository vegetableRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public VegetableResponse createVegetable(VegetableRequest request) {
        validateRequest(request);

        String name = request.getName().trim();

        if (vegetableRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Vegetable already exists with name: " + name);
        }

        Category category = getActiveCategory(request.getCategoryId());

        Vegetable vegetable = Vegetable.builder()
                .name(name)
                .description(trimToNull(request.getDescription()))
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim())
                .imageUrl(trimToNull(request.getImageUrl()))
                .active(true)
                .category(category)
                .build();

        return mapToResponse(vegetableRepository.save(vegetable));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VegetableResponse> getAllVegetables() {
        return vegetableRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VegetableResponse getVegetableById(Long id) {
        validateId(id);

        Vegetable vegetable = vegetableRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Vegetable not found with ID: " + id));

        return mapToResponse(vegetable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VegetableResponse> getVegetablesByCategory(Long categoryId) {
        validateId(categoryId);

        if (!categoryRepository.findByIdAndActiveTrue(categoryId).isPresent()) {
            throw new RuntimeException("Category not found with ID: " + categoryId);
        }

        return vegetableRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public VegetableResponse updateVegetable(Long id, VegetableRequest request) {
        validateId(id);
        validateRequest(request);

        Vegetable vegetable = vegetableRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Vegetable not found with ID: " + id));

        String name = request.getName().trim();

        if (vegetableRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new RuntimeException("Vegetable already exists with name: " + name);
        }

        Category category = getActiveCategory(request.getCategoryId());

        vegetable.setName(name);
        vegetable.setDescription(trimToNull(request.getDescription()));
        vegetable.setPrice(request.getPrice());
        vegetable.setQuantity(request.getQuantity());
        vegetable.setUnit(request.getUnit().trim());
        vegetable.setImageUrl(trimToNull(request.getImageUrl()));
        vegetable.setCategory(category);

        return mapToResponse(vegetableRepository.save(vegetable));
    }

    @Override
    public void deleteVegetable(Long id) {
        validateId(id);

        Vegetable vegetable = vegetableRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Vegetable not found with ID: " + id));

        vegetable.setActive(false);
        vegetableRepository.save(vegetable);
    }

    private Category getActiveCategory(Long categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
                .orElseThrow(() -> new RuntimeException(
                        "Active category not found with ID: " + categoryId));
    }

    private void validateRequest(VegetableRequest request) {
        if (request == null) {
            throw new RuntimeException("Vegetable request cannot be null");
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Vegetable name is required");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Quantity cannot be negative");
        }

        if (request.getUnit() == null || request.getUnit().trim().isEmpty()) {
            throw new RuntimeException("Unit is required");
        }

        if (request.getCategoryId() == null || request.getCategoryId() <= 0) {
            throw new RuntimeException("Valid category ID is required");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Invalid vegetable/category ID");
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private VegetableResponse mapToResponse(Vegetable vegetable) {
        return VegetableResponse.builder()
                .id(vegetable.getId())
                .name(vegetable.getName())
                .description(vegetable.getDescription())
                .price(vegetable.getPrice())
                .quantity(vegetable.getQuantity())
                .unit(vegetable.getUnit())
                .imageUrl(vegetable.getImageUrl())
                .active(vegetable.getActive())
                .categoryId(vegetable.getCategory() != null ? vegetable.getCategory().getId() : null)
                .categoryName(vegetable.getCategory() != null ? vegetable.getCategory().getName() : null)
                .createdAt(vegetable.getCreatedAt())
                .updatedAt(vegetable.getUpdatedAt())
                .build();
    }
}
