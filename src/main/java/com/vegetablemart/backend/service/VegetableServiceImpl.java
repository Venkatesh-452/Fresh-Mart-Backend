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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VegetableServiceImpl implements VegetableService {

    private final VegetableRepository vegetableRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public VegetableResponse createVegetable(VegetableRequest request) {

        // 1. Check duplicate vegetable name
        if (vegetableRepository.existsByName(request.getName())) {
            throw new RuntimeException(
                    "Vegetable already exists with name: " + request.getName()
            );
        }

        // 2. Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(
                        "Category not found with ID: " + request.getCategoryId()
                ));

        // 3. Create Vegetable entity
        Vegetable vegetable = Vegetable.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .imageUrl(request.getImageUrl())
                .active(true)
                .category(category)
                .build();

        // 4. Save vegetable
        Vegetable savedVegetable = vegetableRepository.save(vegetable);

        // 5. Convert Entity → Response DTO
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
    public VegetableResponse getVegetableById(Long id) {

        Vegetable vegetable = vegetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vegetable not found with ID: " + id
                ));

        return mapToResponse(vegetable);
    }

    @Override
    public VegetableResponse updateVegetable(
            Long id,
            VegetableRequest request
    ) {

        // 1. Find existing vegetable
        Vegetable vegetable = vegetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vegetable not found with ID: " + id
                ));

        // 2. Check duplicate name
        if (!vegetable.getName().equalsIgnoreCase(request.getName())
                && vegetableRepository.existsByName(request.getName())) {

            throw new RuntimeException(
                    "Vegetable already exists with name: " + request.getName()
            );
        }

        // 3. Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException(
                        "Category not found with ID: " + request.getCategoryId()
                ));

        // 4. Update fields
        vegetable.setName(request.getName());
        vegetable.setDescription(request.getDescription());
        vegetable.setPrice(request.getPrice());
        vegetable.setQuantity(request.getQuantity());
        vegetable.setUnit(request.getUnit());
        vegetable.setImageUrl(request.getImageUrl());
        vegetable.setCategory(category);

        // 5. Save updated vegetable
        Vegetable updatedVegetable = vegetableRepository.save(vegetable);

        // 6. Convert Entity → Response
        return mapToResponse(updatedVegetable);
    }

    @Override
    public void deleteVegetable(Long id) {

        // 1. Check if vegetable exists
        Vegetable vegetable = vegetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Vegetable not found with ID: " + id
                ));

        // 2. Delete vegetable
        vegetableRepository.delete(vegetable);
    }

    // Entity → Response DTO
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