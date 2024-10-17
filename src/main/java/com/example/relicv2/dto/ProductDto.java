package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private List<String> imageUrls;
    private String condition;
    private String shortDescription;
    private String description;
    private Long currentBid;
    private String biddingTimeLimit;
    private String vendorShopName;
    private Long vendorId;
    private String productImageUrl;
    private Long minimumBid;
    private String createdAt;
    private int numberOfBids;
    private Double averageRating;
    private Long categoryId;
    private String categoryName;
    private Long winningBidderId;
    private boolean winningBidderTrophy;
}
