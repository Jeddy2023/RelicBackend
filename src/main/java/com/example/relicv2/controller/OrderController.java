package com.example.relicv2.controller;

import com.example.relicv2.dto.*;
import com.example.relicv2.enums.OrderStatus;
import com.example.relicv2.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/user")
    public ResponseEntity<Page<OrderUserDto>> getUserOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "minTotalPrice", required = false) Double minTotalPrice,
            @RequestParam(value = "maxTotalPrice", required = false) Double maxTotalPrice,
            @RequestParam(value = "createdAtStart", required = false) String createdAtStart,
            @RequestParam(required = false) String keyword,
            @RequestParam(value = "createdAtEnd", required = false) String createdAtEnd,
            @RequestParam(value = "sortBy", required = false, defaultValue = "Date Descending") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId
    ) {
        List<OrderUserDto> orders = orderService.getUserOrders(status, minTotalPrice, maxTotalPrice, createdAtStart, createdAtEnd, sortBy, userId, keyword);

        int start = Math.min(page * size, orders.size());
        int end = Math.min((page + 1) * size, orders.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderUserDto> orderPage = new PageImpl<>(orders.subList(start, end), pageable, orders.size());

        return new ResponseEntity<>(orderPage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderDto createOrderDto) {
        orderService.createOrder(createOrderDto);
        return new ResponseEntity<>("Order created successfully", HttpStatus.CREATED);
    }

    @PutMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestBody CancelOrderDto cancelOrderDto) {
        String response = orderService.cancelOrder(cancelOrderDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "vendorPaymentSent", required = false) Boolean vendorPaymentSent,
            @RequestParam(value = "paymentReceived", required = false) Boolean paymentReceived,
            @RequestParam(value = "minCompanyCommission", required = false) Double minCompanyCommission,
            @RequestParam(value = "maxCompanyCommission", required = false) Double maxCompanyCommission,
            @RequestParam(value = "minCompanyPercentage", required = false) Double minCompanyPercentage,
            @RequestParam(value = "maxCompanyPercentage", required = false) Double maxCompanyPercentage,
            @RequestParam(value = "minTotalPrice", required = false) Double minTotalPrice,
            @RequestParam(value = "maxTotalPrice", required = false) Double maxTotalPrice,
            @RequestParam(value = "createdAtStart", required = false) String createdAtStart,
            @RequestParam(value = "createdAtEnd", required = false) String createdAtEnd,
            @RequestParam(value = "sortBy", required = false, defaultValue = "Date Descending") String sortBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<OrderDto> orders = orderService.getAllOrders(status, vendorPaymentSent, paymentReceived, minCompanyCommission, maxCompanyCommission, minCompanyPercentage, maxCompanyPercentage, minTotalPrice, maxTotalPrice, createdAtStart, createdAtEnd, sortBy, keyword);

        int start = Math.min(page * size, orders.size());
        int end = Math.min((page + 1) * size, orders.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orderPage = new PageImpl<>(orders.subList(start, end), pageable, orders.size());

        return new ResponseEntity<>(orderPage, HttpStatus.OK);
    }

    @PutMapping("/mark-payment-received/{orderId}")
    public ResponseEntity<String> markPaymentAsReceived(@PathVariable Long orderId) {
        orderService.markPaymentAsReceived(orderId);
        return new ResponseEntity<>("Payment marked as received", HttpStatus.OK);
    }

    @PutMapping("/mark-shipped/{orderId}")
    public ResponseEntity<String> markOrderAsShipped(@PathVariable Long orderId) {
        orderService.markOrderAsShipped(orderId);
        return new ResponseEntity<>("Order marked as shipped", HttpStatus.OK);
    }

    @PutMapping("/mark-delivered/{orderId}")
    public ResponseEntity<String> markOrderAsDelivered(@PathVariable Long orderId) {
        orderService.markOrderAsDelivered(orderId);
        return new ResponseEntity<>("Order marked as delivered", HttpStatus.OK);
    }

    @PutMapping("/mark-vendor-payment-sent/{orderId}")
    public ResponseEntity<String> markVendorPaymentSent(@PathVariable Long orderId) {
        orderService.markVendorPaymentSent(orderId);
        return new ResponseEntity<>("Vendor payment marked as sent", HttpStatus.OK);
    }

    @GetMapping("/total")
    public ResponseEntity<TotalOrdersCountDto> getTotalOrdersCount() {
        TotalOrdersCountDto totalOrdersCountDto = orderService.getTotalOrderCounts();
        return new ResponseEntity<>(totalOrdersCountDto, HttpStatus.OK);
    }
}
