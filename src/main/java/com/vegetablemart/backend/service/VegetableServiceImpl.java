package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.vegetable.VegetableRequest;
import com.vegetablemart.backend.dto.vegetable.VegetableResponse;
import com.vegetablemart.backend.entity.Category;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.DuplicateResourceException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
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

    // =========================================================
    // CREATE VEGETABLE
    // =========================================================

    @Override
    public VegetableResponse createVegetable(
            VegetableRequest request
    ) {

        validateRequest(request);

        String name = request.getName().trim();

        if (vegetableRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Vegetable already exists with name: " + name
            );
        }

        Category category =
                getActiveCategory(request.getCategoryId());

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

        return mapToResponse(
                vegetableRepository.save(vegetable)
        );
    }

    // =========================================================
    // GET ALL ACTIVE VEGETABLES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<VegetableResponse> getAllVegetables() {

        return vegetableRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET VEGETABLE BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public VegetableResponse getVegetableById(Long id) {

        validateId(id, "vegetable");

        return mapToResponse(findActiveVegetable(id));
    }

    // =========================================================
    // GET VEGETABLES BY CATEGORY
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<VegetableResponse> getVegetablesByCategory(
            Long categoryId
    ) {

        validateId(categoryId, "category");

        getActiveCategory(categoryId);

        return vegetableRepository
                .findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // UPDATE VEGETABLE
    // =========================================================

    @Override
    public VegetableResponse updateVegetable(
            Long id,
            VegetableRequest request
    ) {

        validateId(id, "vegetable");
        validateRequest(request);

        Vegetable vegetable = findActiveVegetable(id);

        String name = request.getName().trim();

        if (vegetableRepository
                .existsByNameIgnoreCaseAndIdNot(name, id)) {

            throw new DuplicateResourceException(
                    "Vegetable already exists with name: " + name
            );
        }

        Category category =
                getActiveCategory(request.getCategoryId());

        vegetable.setName(name);
        vegetable.setDescription(
                trimToNull(request.getDescription())
        );
        vegetable.setPrice(request.getPrice());
        vegetable.setQuantity(request.getQuantity());
        vegetable.setUnit(request.getUnit().trim());
        vegetable.setImageUrl(
                trimToNull(request.getImageUrl())
        );
        vegetable.setCategory(category);

        return mapToResponse(
                vegetableRepository.save(vegetable)
        );
    }

    // =========================================================
    // SOFT DELETE VEGETABLE
    // =========================================================

    @Override
    public void deleteVegetable(Long id) {

        validateId(id, "vegetable");

        Vegetable vegetable = findActiveVegetable(id);

        vegetable.setActive(false);

        vegetableRepository.save(vegetable);
    }

    // =========================================================
    // FIND ACTIVE VEGETABLE
    // =========================================================

    private Vegetable findActiveVegetable(Long id) {

        return vegetableRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vegetable not found with ID: " + id
                        )
                );
    }

    // =========================================================
    // GET ACTIVE CATEGORY
    // =========================================================

    private Category getActiveCategory(Long categoryId) {

        if (categoryId == null || categoryId <= 0) {
            throw new BadRequestException(
                    "Valid category ID is required"
            );
        }

        return categoryRepository
                .findByIdAndActiveTrue(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active category not found with ID: "
                                        + categoryId
                        )
                );
    }

    // =========================================================
    // VALIDATE REQUEST
    // =========================================================

    private void validateRequest(
            VegetableRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    "Vegetable request cannot be null"
            );
        }

        if (request.getName() == null
                || request.getName().trim().isEmpty()) {

            throw new BadRequestException(
                    "Vegetable name is required"
            );
        }

        if (request.getName().trim().length() > 150) {
            throw new BadRequestException(
                    "Vegetable name cannot exceed 150 characters"
            );
        }

        if (request.getPrice() == null
                || request.getPrice()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException(
                    "Price must be greater than 0"
            );
        }

        if (request.getQuantity() == null
                || request.getQuantity()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new BadRequestException(
                    "Quantity cannot be negative"
            );
        }

        if (request.getUnit() == null
                || request.getUnit().trim().isEmpty()) {

            throw new BadRequestException(
                    "Unit is required"
            );
        }

        if (request.getCategoryId() == null
                || request.getCategoryId() <= 0) {

            throw new BadRequestException(
                    "Valid category ID is required"
            );
        }
    }

    // =========================================================
    // VALIDATE ID
    // =========================================================

    private void validateId(
            Long id,
            String resourceName
    ) {

        if (id == null || id <= 0) {
            throw new BadRequestException(
                    "Invalid " + resourceName + " ID"
            );
        }
    }

    // =========================================================
    // TRIM TO NULL
    // =========================================================

    private String trimToNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    private VegetableResponse mapToResponse(
            Vegetable vegetable
    ) {

        return VegetableResponse.builder()
                .id(vegetable.getId())
                .name(vegetable.getName())
                .description(vegetable.getDescription())
                .price(vegetable.getPrice())
                .quantity(vegetable.getQuantity())
                .unit(vegetable.getUnit())
                .imageUrl(vegetable.getImageUrl())
                .active(vegetable.getActive())
                .categoryId(
                        vegetable.getCategory() != null
                                ? vegetable.getCategory().getId()
                                : null
                )
                .categoryName(
                        vegetable.getCategory() != null
                                ? vegetable.getCategory().getName()
                                : null
                )
                .createdAt(vegetable.getCreatedAt())
                .updatedAt(vegetable.getUpdatedAt())
                .build();
    }
}
