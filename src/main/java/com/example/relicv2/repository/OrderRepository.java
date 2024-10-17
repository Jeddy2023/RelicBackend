package com.example.relicv2.repository;

import com.example.relicv2.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    long countOrdersInDateRange(Date startDate, Date endDate);

    @Query("SELECT SUM(o.companyCommission) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Double getTotalRevenueInDateRange(Date startDate, Date endDate);

    @Query("SELECT p.id, p.name, COUNT(o.id) FROM Order o JOIN o.product p GROUP BY p.id, p.name ORDER BY COUNT(o.id) DESC")
    List<Object[]> findTopSellingProducts();

    // Additional query if needed for bidding activity
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    long countBidsInDateRange(Date startDate, Date endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE (o.customer.id = :userId OR o.vendor.id = :userId) AND o.createdAt BETWEEN :startDate AND :endDate")
    long countOrdersByUserInDateRange(@Param("userId") Long userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT SUM(o.companyCommission) FROM Order o WHERE o.status IN ('PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED')")
    Double findTotalRevenue();
}
