package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDto {
    private Long productId;
    private Long vendorId;
    private Long customerId;
    private Long totalPrice;
    private Double companyPercentage;
    private Double companyCommission;
}
