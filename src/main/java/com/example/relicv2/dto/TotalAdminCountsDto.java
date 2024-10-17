package com.example.relicv2.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TotalAdminCountsDto {
    private long totalUsers;
    private long totalProducts;
    private long totalBids;
    private long totalOrders;
    private double totalRevenue;
    private long totalVendors;
}
