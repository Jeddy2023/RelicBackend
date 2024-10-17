package com.example.relicv2.service;


import com.example.relicv2.dto.CreateShippingAddressDTO;

public interface ShippingAddressService {
    String saveAddress(CreateShippingAddressDTO createShippingAddressDTO);
    CreateShippingAddressDTO getUserShippingAddress();
    CreateShippingAddressDTO getUserShippingAddressById(Long userId);
}
