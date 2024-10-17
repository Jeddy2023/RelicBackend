package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.MonthDataDto;
import com.example.relicv2.dto.MonthDoubleDataDto;
import com.example.relicv2.dto.ProductAnalyticsDto;
import com.example.relicv2.dto.ProductCategoryRevenueDto;
import com.example.relicv2.exception.BiddingUnauthorizedException;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.OrderRepository;
import com.example.relicv2.repository.ProductRepository;
import com.example.relicv2.repository.TransactionRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.AnalyticsService;
import com.example.relicv2.util.UserAuthenticationHelper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@AllArgsConstructor
public class AnalyticsServiceImple implements AnalyticsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    private UserAuthenticationHelper userAuthenticationHelper;

    @Override
    public List<MonthDataDto> generateLast12MonthsUserData() {
        List<MonthDataDto> last12Months = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.DAY_OF_MONTH, -i * 28);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.DAY_OF_MONTH, -28);

            String monthYear = dateFormat.format(endDate.getTime());
            long users = userRepository.countUsersInDateRange(startDate.getTime(), endDate.getTime());

            last12Months.add(new MonthDataDto(monthYear, users));
        }
        return last12Months;
    }

    @Override
    public List<MonthDataDto> generateLast12MonthsOrderData() {
        List<MonthDataDto> last12MonthsOrderData = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.MONTH, -i);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.MONTH, -1);

            String monthYear = dateFormat.format(endDate.getTime());
            long orders = orderRepository.countOrdersInDateRange(startDate.getTime(), endDate.getTime());

            last12MonthsOrderData.add(new MonthDataDto(monthYear, orders));
        }
        return last12MonthsOrderData;
    }

    @Override
    public List<MonthDoubleDataDto> generateLast12MonthsRevenueData() {
        List<MonthDoubleDataDto> last12MonthsRevenueData = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.MONTH, -i);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.MONTH, -1);

            String monthYear = dateFormat.format(endDate.getTime());
            Double revenue = orderRepository.getTotalRevenueInDateRange(startDate.getTime(), endDate.getTime());
            revenue = revenue != null ? revenue : 0.0;

            last12MonthsRevenueData.add(new MonthDoubleDataDto(monthYear, revenue));
        }
        return last12MonthsRevenueData;
    }


    @Override
    public List<ProductAnalyticsDto> getTopSellingProducts() {
        List<ProductAnalyticsDto> topSellingProducts = new ArrayList<>();

        List<Object[]> results = orderRepository.findTopSellingProducts();

        for (Object[] result : results) {
            Long productId = (Long) result[0];
            String productName = (String) result[1];
            Long totalOrders = (Long) result[2];

            ProductAnalyticsDto productDto = new ProductAnalyticsDto(productId, productName, totalOrders);
            topSellingProducts.add(productDto);
        }

        return topSellingProducts;
    }

    @Override
    public List<MonthDataDto> getBiddingActivityOverTime() {
        List<MonthDataDto> biddingActivityOverTime = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.MONTH, -i);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.MONTH, -1);

            String monthYear = dateFormat.format(endDate.getTime());
            long bidsPlaced = orderRepository.countBidsInDateRange(startDate.getTime(), endDate.getTime());

            biddingActivityOverTime.add(new MonthDataDto(monthYear, bidsPlaced));
        }
        return biddingActivityOverTime;
    }

    @Override
    public List<MonthDoubleDataDto> getAverageTransactionAmountOverTime() {
        List<MonthDoubleDataDto> averageTransactionAmountOverTime = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.MONTH, -i);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.MONTH, -1);

            String monthYear = dateFormat.format(endDate.getTime());
            Double avgTransactionAmount = transactionRepository.calculateAverageTransactionAmount(startDate.getTime(), endDate.getTime());
            avgTransactionAmount = avgTransactionAmount != null ? avgTransactionAmount : 0.0;

            averageTransactionAmountOverTime.add(new MonthDoubleDataDto(monthYear, avgTransactionAmount));
        }
        return averageTransactionAmountOverTime;
    }

    @Override
    public List<ProductCategoryRevenueDto> getRevenueByProductCategory() {
        List<ProductCategoryRevenueDto> revenueByProductCategory = new ArrayList<>();

        List<Object[]> results = productRepository.calculateRevenueByCategory();

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Double revenue = (Double) result[1];

            revenueByProductCategory.add(new ProductCategoryRevenueDto(categoryName, revenue));
        }

        return revenueByProductCategory;
    }


    @Override
    public List<MonthDataDto> generateLast12MonthsUserOrderData() {
        User user = userAuthenticationHelper.getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }
        List<MonthDataDto> last12MonthsOrderData = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");

        for (int i = 11; i >= 0; i--) {
            Calendar endDate = (Calendar) currentDate.clone();
            endDate.add(Calendar.MONTH, -i);
            Calendar startDate = (Calendar) endDate.clone();
            startDate.add(Calendar.MONTH, -1);

            String monthYear = dateFormat.format(endDate.getTime());
            long orders = orderRepository.countOrdersByUserInDateRange(user.getId(), startDate.getTime(), endDate.getTime());

            last12MonthsOrderData.add(new MonthDataDto(monthYear, orders));
        }
        return last12MonthsOrderData;
    }

    @Override
    public List<ProductAnalyticsDto> getUserTopSellingProducts() {
        User user = userAuthenticationHelper.getLoggedInUser();
        if (user == null) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }
        List<ProductAnalyticsDto> topSellingProducts = new ArrayList<>();
        List<Object[]> results = productRepository.findTopSellingProductsByUser(user.getId());

        for (Object[] result : results) {
            Long productId = (Long) result[0];
            String productName = (String) result[1];
            Long totalOrders = (Long) result[2];

            ProductAnalyticsDto productDto = new ProductAnalyticsDto(productId, productName, totalOrders);
            topSellingProducts.add(productDto);
        }

        return topSellingProducts;
    }
}
