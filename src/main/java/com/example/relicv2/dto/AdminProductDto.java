package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDto {
    private Long id;
    private String name;
    private String shortDescription;
    private String description;
    private Long currentBid;
    private String biddingTimeLimit;
    private String vendorShopName;
    private String productImageUrl;
    private List<String> imageUrls;
    private Long minimumBid;
    private String createdAt;
    private String updatedAt;
    private boolean isBiddingClosed;
    private boolean isBlocked;
    private int numberOfBids;
    private Double averageRating;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private String winningBidderUsername;
}
