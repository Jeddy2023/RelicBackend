package com.example.relicv2.service.implementations;

import com.example.relicv2.dto.TransactionDto;
import com.example.relicv2.dto.TransactionRequestDto;
import com.example.relicv2.enums.OrderStatus;
import com.example.relicv2.enums.TransactionType;
import com.example.relicv2.exception.ResourceNotFoundException;
import com.example.relicv2.model.Order;
import com.example.relicv2.model.Transaction;
import com.example.relicv2.model.User;
import com.example.relicv2.repository.OrderRepository;
import com.example.relicv2.repository.TransactionRepository;
import com.example.relicv2.repository.UserRepository;
import com.example.relicv2.service.OrderService;
import com.example.relicv2.service.TransactionService;
import com.example.relicv2.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TransactionServiceImple implements TransactionService {
    private ModelMapper modelMapper;
    private UserAuthenticationHelper userAuthenticationHelper;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Override
    public void createCustomerPaymentTransaction(TransactionRequestDto transactionRequestDto) {
        Optional<Order> order = orderRepository.findById(transactionRequestDto.getOrderId());
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }

        User user = order.get().getCustomer();

        order.get().setStatus(OrderStatus.PAID);
        order.get().setPaymentReceived(true);
        orderRepository.save(order.get());

        Transaction transaction = new Transaction();
        transaction.setOrder(order.get());
        transaction.setUser(user);
        transaction.setAmount(transactionRequestDto.getAmount());
        transaction.setPaystackReference(transactionRequestDto.getPaystackReference());
        transaction.setTransactionType(TransactionType.PAYMENT);
        transactionRepository.save(transaction);

        orderService.markPaymentAsReceived(transactionRequestDto.getOrderId());
    }

    @Override
    public void createVendorDisbursementTransaction(TransactionRequestDto transactionRequestDto) {
        Optional<Order> order = orderRepository.findById(transactionRequestDto.getOrderId());
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }

        User user = order.get().getCustomer();

        order.get().setStatus(OrderStatus.PAID);
        orderRepository.save(order.get());

        Transaction transaction = new Transaction();
        transaction.setOrder(order.get());
        transaction.setUser(user);
        transaction.setAmount(transactionRequestDto.getAmount());
        transaction.setTransactionType(TransactionType.DISBURSEMENT);
        transactionRepository.save(transaction);
    }

    @Override
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return modelMapper.map(transaction, TransactionDto.class);
    }

    @Override
    public List<TransactionDto> getTransactionsByUser(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getTransactionsByOrder(Long orderId) {
        List<Transaction> transactions = transactionRepository.findByOrderId(orderId);
        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDto> getAllTransactions(
            Long userId,
            Long orderId,
            String startDate,
            String endDate,
            String transactionType,
            String sortBy,
            String keyword
    ) {
        List<Transaction> transactions = transactionRepository.findAll();

        if (userId != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        }

        if (orderId != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.isEmpty()) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getUser().getUsername().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        try{
        if (startDate != null && endDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            Date startDateNew = dateFormat.parse(startDate);
            Date endDateNew = dateFormat.parse(endDate);
            transactions = transactions.stream()
                    .filter(transaction ->
                            !transaction.getTransactionDate().before(startDateNew) &&
                                    !transaction.getTransactionDate().after(endDateNew))
                    .collect(Collectors.toList());
        }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (transactionType != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getTransactionType().toString().equals(transactionType))
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "amountAsc":
                transactions.sort(Comparator.comparingDouble(Transaction::getAmount));
                break;
            case "amountDesc":
                transactions.sort(Comparator.comparingDouble(Transaction::getAmount).reversed());
                break;
            case "dateAsc":
                transactions.sort(Comparator.comparing(Transaction::getTransactionDate));
                break;
            case "dateDesc":
                transactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());
                break;
        }

        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
