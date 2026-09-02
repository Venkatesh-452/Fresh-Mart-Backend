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
    public VegetableResponse createVegetable(
            VegetableRequest request
    ) {

        validateRequest(request);

        String name = request.getName().trim();

        // Check duplicate vegetable name
        if (vegetableRepository.existsByName(name)) {
            throw new RuntimeException(
                    "Vegetable already exists with name: " + name
            );
        }

        // Find category
        Category category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: "
                                                + request.getCategoryId()
                                )
                        );

        Vegetable vegetable =
                Vegetable.builder()
                        .name(name)
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .quantity(request.getQuantity())
                        .unit(request.getUnit().trim())
                        .imageUrl(request.getImageUrl())
                        .active(true)
                        .category(category)
                        .build();

        Vegetable savedVegetable =
                vegetableRepository.save(vegetable);

        return mapToResponse(savedVegetable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VegetableResponse> getAllVegetables() {

        return vegetableRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VegetableResponse getVegetableById(
            Long id
    ) {

        if (id == null || id <= 0) {
            throw new RuntimeException(
                    "Invalid vegetable ID"
            );
        }

        Vegetable vegetable =
                vegetableRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vegetable not found with ID: "
                                                + id
                                )
                        );

        return mapToResponse(vegetable);
    }

    @Override
    public VegetableResponse updateVegetable(
            Long id,
            VegetableRequest request
    ) {

        validateRequest(request);

        if (id == null || id <= 0) {
            throw new RuntimeException(
                    "Invalid vegetable ID"
            );
        }

        Vegetable vegetable =
                vegetableRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vegetable not found with ID: "
                                                + id
                                )
                        );

        String name = request.getName().trim();

        // Check duplicate name
        if (!vegetable.getName().equalsIgnoreCase(name)
                && vegetableRepository.existsByName(name)) {

            throw new RuntimeException(
                    "Vegetable already exists with name: "
                            + name
            );
        }

        Category category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found with ID: "
                                                + request.getCategoryId()
                                )
                        );

        vegetable.setName(name);
        vegetable.setDescription(
                request.getDescription()
        );
        vegetable.setPrice(
                request.getPrice()
        );
        vegetable.setQuantity(
                request.getQuantity()
        );
        vegetable.setUnit(
                request.getUnit().trim()
        );
        vegetable.setImageUrl(
                request.getImageUrl()
        );
        vegetable.setCategory(category);

        Vegetable updatedVegetable =
                vegetableRepository.save(vegetable);

        return mapToResponse(updatedVegetable);
    }

    @Override
    public void deleteVegetable(Long id) {

        if (id == null || id <= 0) {
            throw new RuntimeException(
                    "Invalid vegetable ID"
            );
        }

        Vegetable vegetable =
                vegetableRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vegetable not found with ID: "
                                                + id
                                )
                        );

        /*
         * Prefer soft delete for e-commerce applications.
         * This preserves historical cart/order references.
         */
        vegetable.setActive(false);

        vegetableRepository.save(vegetable);
    }

    private void validateRequest(
            VegetableRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Vegetable request cannot be null"
            );
        }

        if (request.getName() == null
                || request.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Vegetable name is required"
            );
        }

        if (request.getPrice() == null
                || request.getPrice()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Price cannot be negative"
            );
        }

        if (request.getQuantity() == null
                || request.getQuantity()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new RuntimeException(
                    "Quantity cannot be negative"
            );
        }

        if (request.getUnit() == null
                || request.getUnit().trim().isEmpty()) {

            throw new RuntimeException(
                    "Unit is required"
            );
        }

        if (request.getCategoryId() == null
                || request.getCategoryId() <= 0) {

            throw new RuntimeException(
                    "Valid category ID is required"
            );
        }
    }

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
                .createdAt(
                        vegetable.getCreatedAt()
                )
                .updatedAt(
                        vegetable.getUpdatedAt()
                )
                .build();
    }
}