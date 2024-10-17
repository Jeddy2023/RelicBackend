package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBiddingListDto {
    private Long id;
    private Long bidAmount;
    private Date createdAt;
    private String productName;
    private String productCurrentBid;
    private Long productId;
}
