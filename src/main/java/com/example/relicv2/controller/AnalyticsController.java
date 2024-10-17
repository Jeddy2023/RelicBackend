package com.example.relicv2.controller;

import com.example.relicv2.dto.MonthDataDto;
import com.example.relicv2.dto.MonthDoubleDataDto;
import com.example.relicv2.dto.ProductAnalyticsDto;
import com.example.relicv2.dto.ProductCategoryRevenueDto;
import com.example.relicv2.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/users")
    public ResponseEntity<List<MonthDataDto>> getUsersAnalytics() {
        List<MonthDataDto> users = analyticsService.generateLast12MonthsUserData();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<MonthDataDto>> getOrdersAnalytics() {
        List<MonthDataDto> orders = analyticsService.generateLast12MonthsOrderData();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<MonthDoubleDataDto>> getRevenueAnalytics() {
        List<MonthDoubleDataDto> revenue = analyticsService.generateLast12MonthsRevenueData();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/top-selling-products")
    public ResponseEntity<List<ProductAnalyticsDto>> getTopSellingProductsAnalytics() {
        List<ProductAnalyticsDto> topSellingProducts = analyticsService.getTopSellingProducts();
        return ResponseEntity.ok(topSellingProducts);
    }

    @GetMapping("/bidding-activity")
    public ResponseEntity<List<MonthDataDto>> getBiddingActivityAnalytics() {
        List<MonthDataDto> biddingActivity = analyticsService.getBiddingActivityOverTime();
        return ResponseEntity.ok(biddingActivity);
    }

    @GetMapping("/average-transaction-amount")
    public ResponseEntity<List<MonthDoubleDataDto>> getAverageTransactionAmountAnalytics() {
        List<MonthDoubleDataDto> avgTransactionAmount = analyticsService.getAverageTransactionAmountOverTime();
        return ResponseEntity.ok(avgTransactionAmount);
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<List<ProductCategoryRevenueDto>> getRevenueByProductCategoryAnalytics() {
        List<ProductCategoryRevenueDto> revenueByCategory = analyticsService.getRevenueByProductCategory();
        return ResponseEntity.ok(revenueByCategory);
    }

    @GetMapping("/user-orders")
    public ResponseEntity<List<MonthDataDto>> getUserOrdersAnalytics() {
        List<MonthDataDto> orders = analyticsService.generateLast12MonthsUserOrderData();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user-top-selling-products")
    public ResponseEntity<List<ProductAnalyticsDto>> getUserTopSellingProductsAnalytics() {
        List<ProductAnalyticsDto> topSellingProducts = analyticsService.getUserTopSellingProducts();
        return ResponseEntity.ok(topSellingProducts);
    }
}
