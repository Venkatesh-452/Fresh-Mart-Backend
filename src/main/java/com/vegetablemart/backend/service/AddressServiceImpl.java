package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.address.AddressRequest;
import com.vegetablemart.backend.dto.address.AddressResponse;
import com.vegetablemart.backend.entity.Address;
import com.vegetablemart.backend.entity.User;
import com.vegetablemart.backend.repository.AddressRepository;
import com.vegetablemart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public AddressResponse createAddress(String email, AddressRequest request) {
        User user = getUser(email);

        boolean makeDefault = Boolean.TRUE.equals(request.getDefaultAddress())
                || !addressRepository.findByUserIdAndDefaultAddressTrue(user.getId()).isPresent();

        if (makeDefault) {
            clearDefaultAddress(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName().trim())
                .phone(request.getPhone().trim())
                .addressLine(request.getAddressLine().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .landmark(normalize(request.getLandmark()))
                .defaultAddress(makeDefault)
                .build();

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(String email) {
        User user = getUser(email);

        return addressRepository
                .findByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(String email, Long addressId) {
        User user = getUser(email);
        validateId(addressId);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        return mapToResponse(address);
    }

    @Override
    public AddressResponse updateAddress(String email, Long addressId, AddressRequest request) {
        User user = getUser(email);
        validateId(addressId);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean makeDefault = Boolean.TRUE.equals(request.getDefaultAddress());

        if (makeDefault) {
            clearDefaultAddress(user.getId());
        } else if (Boolean.TRUE.equals(address.getDefaultAddress())) {
            // Keep the existing default address unless another address is selected.
            makeDefault = true;
        }

        address.setFullName(request.getFullName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine(request.getAddressLine().trim());
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());
        address.setLandmark(normalize(request.getLandmark()));
        address.setDefaultAddress(makeDefault);

        return mapToResponse(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(String email, Long addressId) {
        User user = getUser(email);
        validateId(addressId);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean wasDefault = Boolean.TRUE.equals(address.getDefaultAddress());
        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Override
    public AddressResponse setDefaultAddress(String email, Long addressId) {
        User user = getUser(email);
        validateId(addressId);

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        clearDefaultAddress(user.getId());
        address.setDefaultAddress(true);

        return mapToResponse(addressRepository.save(address));
    }

    private User getUser(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Authenticated user is required");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserIdAndDefaultAddressTrue(userId)
                .ifPresent(address -> {
                    address.setDefaultAddress(false);
                    addressRepository.save(address);
                });
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new RuntimeException("Address ID must be positive");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .landmark(address.getLandmark())
                .defaultAddress(address.getDefaultAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
