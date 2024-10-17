package com.example.relicv2.service;

import com.example.relicv2.dto.TransactionDto;
import com.example.relicv2.dto.TransactionRequestDto;

import java.util.List;

public interface TransactionService {
    void createCustomerPaymentTransaction(TransactionRequestDto transactionRequestDto);
    void createVendorDisbursementTransaction(TransactionRequestDto transactionRequestDto);
    TransactionDto getTransactionById(Long id);
    List<TransactionDto> getTransactionsByUser(Long userId);
    List<TransactionDto> getTransactionsByOrder(Long orderId);
    List<TransactionDto> getAllTransactions(
            Long userId,
            Long orderId,
            String startDate,
            String endDate,
            String transactionType,
            String sortBy,
            String keyword
    );
    void deleteTransaction(Long id);
}
