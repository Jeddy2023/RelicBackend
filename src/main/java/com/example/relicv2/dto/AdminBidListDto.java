package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBidListDto {
    private Long id;
    private Date createdAt;
    private String productName;
    private String bidderUsername;
    private Long bidAmount;
}
