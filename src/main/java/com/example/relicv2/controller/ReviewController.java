package com.example.relicv2.controller;

import com.example.relicv2.dto.ApplicationReviewDto;
import com.example.relicv2.dto.ProductReveiwDto;
import com.example.relicv2.dto.ReviewDto;
import com.example.relicv2.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<String> giveProductReview(@RequestBody ProductReveiwDto productReviewDto, @PathVariable Long productId) {
        String response = reviewService.giveproductReview(productReviewDto, productId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/application")
    public ResponseEntity<String> giveApplicationReview(@RequestBody ApplicationReviewDto applicationReviewDto) {
        String response = reviewService.giveApplicationReview(applicationReviewDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<ReviewDto> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/product")
    public ResponseEntity<List<ReviewDto>> getAllProductReviews() {
        List<ReviewDto> reviews = reviewService.getAllProductReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/application")
    public ResponseEntity<List<ReviewDto>> getAllApplicationReviews() {
        List<ReviewDto> reviews = reviewService.getAllApplicationReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDto>> getAllReviewsForProduct(@PathVariable Long productId) {
        List<ReviewDto> reviews = reviewService.getAllReviewForAParticularProduct(productId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
}
