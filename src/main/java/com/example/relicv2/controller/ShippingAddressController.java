package com.example.relicv2.controller;

import com.example.relicv2.dto.CreateShippingAddressDTO;
import com.example.relicv2.service.ShippingAddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shippingaddress")
@CrossOrigin(origins = "*")
public class ShippingAddressController {
    @Autowired
    private ShippingAddressService shippingAddressService;

    @PostMapping
    public ResponseEntity<String> saveShippingAddress(@Valid @RequestBody CreateShippingAddressDTO createShippingAddressDTO) {
        String createdShippingAddress = shippingAddressService.saveAddress(createShippingAddressDTO);
        return new ResponseEntity<>(createdShippingAddress, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CreateShippingAddressDTO> getUserShippingAddress() {
        CreateShippingAddressDTO shippingAddress = shippingAddressService.getUserShippingAddress();
        return new ResponseEntity<>(shippingAddress, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CreateShippingAddressDTO> getUserShippingAddressById(@PathVariable Long userId) {
        CreateShippingAddressDTO shippingAddress = shippingAddressService.getUserShippingAddressById(userId);
        return new ResponseEntity<>(shippingAddress, HttpStatus.OK);
    }
}
