package com.example.relicv2.dto;

import com.example.relicv2.enums.TransactionType;
import lombok.Data;

import java.util.Date;

@Data
public class TransactionDto {
    private Long id;
    private Long orderId;
    private String orderProductName;
    private Long userId;
    private String userUserName;
    private Double amount;
    private TransactionType transactionType;
    private String paystackReference;
    private Date transactionDate;
    private Date updatedAt;
}
