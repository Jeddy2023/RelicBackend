package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProductDto {
    private Long id;
    private String productImageUrl;
    private List<String> imageUrls;
    private String name;
    private int totalReviews;
    private int totalBids;
    private Long currentBid;
    private String updatedAt;
    private String categoryName;
    private Long minimumBid;
    private String condition;
    private boolean isBiddingClosed;
    private boolean isBlocked;
    private String biddingTimeLimit;
    private Long winningBidderId;
}
