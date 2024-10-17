package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShopDetailsDto {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phonenumber;
    private String email;
    private String shopname;
}
