package com.example.relicv2.repository;

import com.example.relicv2.enums.ReviewType;
import com.example.relicv2.model.Product;
import com.example.relicv2.model.Review;
import com.example.relicv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByReviewType (ReviewType reviewType);
    List<Review> findAllByProduct(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
}
