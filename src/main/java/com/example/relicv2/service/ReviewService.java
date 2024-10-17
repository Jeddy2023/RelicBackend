package com.example.relicv2.service;

import com.example.relicv2.dto.ApplicationReviewDto;
import com.example.relicv2.dto.ProductReveiwDto;
import com.example.relicv2.dto.ReviewDto;

import java.util.List;

public interface ReviewService {
    String giveproductReview(ProductReveiwDto productReviewDTO, Long productId);
    String giveApplicationReview(ApplicationReviewDto applicationReviewDTO);
    List<ReviewDto> getAllReviews();
    List<ReviewDto> getAllProductReviews();
    List<ReviewDto> getAllApplicationReviews();
    List<ReviewDto> getAllReviewForAParticularProduct(Long productId);
}
