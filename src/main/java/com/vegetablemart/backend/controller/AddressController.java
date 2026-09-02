package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.address.AddressRequest;
import com.vegetablemart.backend.dto.address.AddressResponse;
import com.vegetablemart.backend.service.AddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Validated
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(
            Authentication authentication) {
        return ResponseEntity.ok(addressService.getMyAddresses(authentication.getName()));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddressById(
            Authentication authentication,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        return ResponseEntity.ok(addressService.getAddressById(authentication.getName(), addressId));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(authentication.getName(), addressId, request));
    }

    @PutMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        return ResponseEntity.ok(addressService.setDefaultAddress(authentication.getName(), addressId));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        addressService.deleteAddress(authentication.getName(), addressId);
        return ResponseEntity.noContent().build();
    }
}
