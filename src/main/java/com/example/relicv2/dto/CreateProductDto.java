package com.example.relicv2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {
    private String username;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Short description is required")
    private String shortDescription;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Condition is required")
    @Pattern(regexp = "NEW|USED", message = "Invalid condition provided")
    private String condition;

    @Positive(message = "Minimum bid must be greater than zero")
    private Long minimumBid;

    @NotBlank(message = "Bidding time limit is required")
    private String biddingTimeLimit;

    @NotNull(message = "Product image is required")
    private MultipartFile productImage;

    private MultipartFile backViewImage;
    private MultipartFile sideViewImage;
    private MultipartFile innerViewImage;
}
