package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.address.AddressRequest;
import com.vegetablemart.backend.dto.address.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse createAddress(String email, AddressRequest request);

    List<AddressResponse> getMyAddresses(String email);

    AddressResponse getAddressById(String email, Long addressId);

    AddressResponse updateAddress(String email, Long addressId, AddressRequest request);

    void deleteAddress(String email, Long addressId);

    AddressResponse setDefaultAddress(String email, Long addressId);
}
