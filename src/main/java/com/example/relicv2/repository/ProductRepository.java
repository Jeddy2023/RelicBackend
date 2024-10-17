package com.example.relicv2.repository;

import com.example.relicv2.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByVendorId(Long vendorId);
    List<Product> findByIsBiddingClosedFalseAndIsBlockedFalse();
    List<Product> findByVendorIdAndIsBiddingClosedFalse(Long vendorId);
    List<Product> findByWinningBidderId(Long winningBidderId);
    @Query("SELECT p.category.name, SUM(o.companyCommission) FROM Order o JOIN o.product p GROUP BY p.category.name")
    List<Object[]> calculateRevenueByCategory();
    @Query("SELECT p.id, p.name, COUNT(o.product.id) AS totalOrders FROM Order o JOIN o.product p WHERE p.vendor.id = :userId GROUP BY p.id, p.name ORDER BY totalOrders DESC")
    List<Object[]> findTopSellingProductsByUser(@Param("userId") Long userId);
}
