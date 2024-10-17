package com.example.relicv2.service;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.Condition;

import java.util.List;

public interface ProductService {
    String createProduct(CreateProductDto createProductDto);
    List<ProductDto> getAllProducts(boolean totalWonProducts,
                                    boolean totalActiveAuctions,
                                    Long categoryId,
                                    Condition condition,
                                    String keyword,
                                    Long minBid,
                                    Long maxBid,
                                    Double minimumBid,
                                    Integer rating,
                                    String sortBy);
    ProductDto getProductById(Long id);
    String deleteProduct(Long id);
    List<UserProductDto> getUserProducts(
            boolean pendingReviews,
            boolean activeProducts,
            boolean offlineBid,
            String keyword,
            Long userId
    );
    TotalProductsCountDto getTotalProductCounts();
    List<AdminProductDto> getAllProductsForAdmin(
            Long categoryId,
            Condition condition,
            String keyword,
            Boolean isBiddingClosed,
            Boolean isBlocked,
            String biddingTimeLimitStart,
            String biddingTimeLimitEnd,
            Long minBid,
            Long maxBid,
            String sortBy
    );
    String createProductWithUsername(CreateProductDto createProductDto);
    String blockProduct(Long productId);
    String unblockProduct(Long productId);
    String endProduct(Long productId);
    String reopenProduct(Long productId);
    String resetWinningBidder(Long productId);
    void winningBidderTrophy(Long productId);
}
