package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.vegetable.VegetableRequest;
import com.vegetablemart.backend.dto.vegetable.VegetableResponse;

import java.util.List;

public interface VegetableService {

    VegetableResponse createVegetable(VegetableRequest request);

    List<VegetableResponse> getAllVegetables();

    VegetableResponse getVegetableById(Long id);

    VegetableResponse updateVegetable(
            Long id,
            VegetableRequest request
    );

    void deleteVegetable(Long id);
}