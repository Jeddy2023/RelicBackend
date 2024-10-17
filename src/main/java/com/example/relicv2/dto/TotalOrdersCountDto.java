package com.example.relicv2.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TotalOrdersCountDto {
    private long totalOrders;
    private long totalPendingOrders;
    private long totalCompletedOrders;
    private long totalCancelledOrders;
}
