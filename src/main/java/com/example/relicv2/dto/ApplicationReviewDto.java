package com.example.relicv2.dto;

import com.example.relicv2.enums.ReviewType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationReviewDto {
    @Enumerated(EnumType.STRING)
    private ReviewType reviewType = ReviewType.APPLICATION_REVIEW;
    private String comment;
    @Size(min = 1, max = 5)
    private double rating;
}
