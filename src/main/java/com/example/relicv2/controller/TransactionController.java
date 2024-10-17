package com.example.relicv2.controller;

import com.example.relicv2.dto.TransactionDto;
import com.example.relicv2.dto.TransactionRequestDto;
import com.example.relicv2.service.TransactionService;
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
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/payment")
    public ResponseEntity<String> createCustomerPaymentTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        transactionService.createCustomerPaymentTransaction(transactionRequestDto);
        return new ResponseEntity<>("Transaction created successfully", HttpStatus.CREATED);
    }

    @PostMapping("/disbursement")
    public ResponseEntity<String> createVendorDisbursementTransaction(@RequestBody TransactionRequestDto transactionRequestDto) {
        transactionService.createVendorDisbursementTransaction(transactionRequestDto);
        return new ResponseEntity<>("Transaction created successfully", HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        TransactionDto transaction = transactionService.getTransactionById(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TransactionDto>> getTransactionsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<TransactionDto> transactions = transactionService.getTransactionsByUser(userId);

        int start = Math.min(page * size, transactions.size());
        int end = Math.min((page + 1) * size, transactions.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactionPage = new PageImpl<>(transactions.subList(start, end), pageable, transactions.size());

        return new ResponseEntity<>(transactionPage, HttpStatus.OK);
    }


    @GetMapping("/all")
    public ResponseEntity<Page<TransactionDto>> getAllTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false, defaultValue = "dateDesc") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<TransactionDto> transactions = transactionService.getAllTransactions(userId, orderId, startDate, endDate, transactionType, sortBy, keyword);

        int start = Math.min(page * size, transactions.size());
        int end = Math.min((page + 1) * size, transactions.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactionPage = new PageImpl<>(transactions.subList(start, end), pageable, transactions.size());

        return new ResponseEntity<>(transactionPage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>("Transaction deleted successfully", HttpStatus.OK);
    }
}
