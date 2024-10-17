package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.ApplicationReviewDto;
import com.example.relicv2.dto.ProductReveiwDto;
import com.example.relicv2.dto.ReviewDto;
import com.example.relicv2.enums.ReviewType;
import com.example.relicv2.exception.BiddingUnauthorizedException;
import com.example.relicv2.exception.CustomException;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.Review;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.BiddingRepository;
import com.example.relicv2.repository.ProductRepository;
import com.example.relicv2.repository.ReviewRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.NotificationService;
import com.example.relicv2.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ReviewServiceImple implements ReviewService {
    private ProductRepository productRepository;
    private ReviewRepository reviewRepository;
    private ModelMapper modelMapper;
    private UserRepository userRepository;
    private BiddingRepository biddingRepository;
    private final NotificationService notificationService;

    @Override
    public String giveproductReview(ProductReveiwDto productReviewDTO, Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();

        if (productReviewDTO.getComment().isEmpty() || productReviewDTO.getRating() <= 0) {
            throw new CustomException("All fields are required");
        }

        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        Optional<Review> existingReview = reviewRepository.findByUserAndProduct(user, product);
        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setComment(productReviewDTO.getComment());
            review.setRating(productReviewDTO.getRating());
            reviewRepository.save(review);
            notificationService.sendNotification("Review updated by " + user.getUsername() + " on " + product.getName(), product.getVendor().getId());
            return "Review updated successfully";
        } else {
            Review review = new Review();
            review.setReviewType(ReviewType.PRODUCT_REVIEW);
            review.setComment(productReviewDTO.getComment());
            review.setRating(productReviewDTO.getRating());
            review.setUser(user);
            review.setProduct(product);
            reviewRepository.save(review);
            notificationService.sendNotification("New review by " + user.getUsername() + " on " + product.getName(), product.getVendor().getId());
            return "Review sent successfully";
        }
    }

    @Override
    public String giveApplicationReview(ApplicationReviewDto applicationReviewDTO) {
        if(applicationReviewDTO.getComment() == null || applicationReviewDTO.getComment().isEmpty() || applicationReviewDTO.getRating() <= 0){
            throw new CustomException("All fields are required");
        }

        User user = getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }

        Review reviews = new Review();
        reviews.setReviewType(ReviewType.APPLICATION_REVIEW);
        reviews.setComment(applicationReviewDTO.getComment());
        reviews.setRating(applicationReviewDTO.getRating());
        reviews.setUser(user);
        reviewRepository.save(reviews);
        return "Review sent successfully";
    }

    @Override
    public List<ReviewDto> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllProductReviews() {
        List<Review> reviews = reviewRepository.findAllByReviewType(ReviewType.PRODUCT_REVIEW);
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllApplicationReviews() {
        List<Review> reviews = reviewRepository.findAllByReviewType(ReviewType.APPLICATION_REVIEW);
        return mapReviewListToDto(reviews);
    }

    @Override
    public List<ReviewDto> getAllReviewForAParticularProduct(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Product product = productOptional.get();

        List<Review> reviews = reviewRepository.findAllByProduct(product);
        return mapReviewListToDto(reviews);
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }
        String name = authentication.getName();
        return userRepository.findByUsernameOrEmail(name, name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<ReviewDto> mapReviewListToDto(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }
        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }
}
