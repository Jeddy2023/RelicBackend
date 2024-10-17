package com.example.relicv2.service;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    void createOrder(CreateOrderDto createOrderDto);

    OrderDto getOrderById(Long orderId);

    String cancelOrder(CancelOrderDto cancelOrderDto);

    List<OrderDto> getAllOrders(OrderStatus status, Boolean vendorPaymentSent, Boolean paymentReceived, Double minCompanyCommission, Double maxCompanyCommission, Double minCompanyPercentage, Double maxCompanyPercentage, Double minTotalPrice, Double maxTotalPrice, String createdAtStart, String createdAtEnd, String sortBy, String keyword);
    public List<OrderUserDto> getUserOrders(
            OrderStatus status,
            Double minTotalPrice,
            Double maxTotalPrice,
            String createdAtStart,
            String createdAtEnd,
            String sortBy,
            Long userId,
            String keyword
    );
    void markOrderAsDelivered(Long orderId);
    void markVendorPaymentSent(Long orderId);
    void markOrderAsShipped(Long orderId);
    void markPaymentAsReceived(Long orderId);
    TotalOrdersCountDto getTotalOrderCounts();
}
