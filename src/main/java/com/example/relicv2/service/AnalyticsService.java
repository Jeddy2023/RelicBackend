package com.example.relicv2.service;

import com.example.relicv2.dto.MonthDataDto;
import com.example.relicv2.dto.MonthDoubleDataDto;
import com.example.relicv2.dto.ProductAnalyticsDto;
import com.example.relicv2.dto.ProductCategoryRevenueDto;

import java.util.List;

public interface AnalyticsService{
    List<MonthDataDto> generateLast12MonthsUserData();
    List<MonthDataDto> generateLast12MonthsOrderData();
    List<MonthDataDto> generateLast12MonthsUserOrderData();
    List<MonthDoubleDataDto> generateLast12MonthsRevenueData();
    List<ProductAnalyticsDto> getTopSellingProducts();
    List<ProductAnalyticsDto> getUserTopSellingProducts();
    List<MonthDataDto> getBiddingActivityOverTime();
    List<MonthDoubleDataDto> getAverageTransactionAmountOverTime();
    List<ProductCategoryRevenueDto> getRevenueByProductCategory();
}
