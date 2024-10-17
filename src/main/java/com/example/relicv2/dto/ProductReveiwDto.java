package com.example.relicv2.dto;

import com.example.relicv2.enums.ReviewType;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReveiwDto {
    private User user;
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType = ReviewType.PRODUCT_REVIEW;
    private Product product;
    private String comment;
    @Size(min = 1, max = 5)
    private double rating;
}
