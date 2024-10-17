package com.example.relicv2.service;


import com.example.relicv2.dto.CreateShopDetailsDto;

public interface ShopDetailsService {
    String saveDetails(CreateShopDetailsDto createShopDetailsDto);
    CreateShopDetailsDto getUserShopDetails();
}
