package com.example.acacia.repository;

import com.example.acacia.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCheckoutRequestID(String checkoutRequestID);

    Optional<Transaction> findByMpesaReceiptNumber(String mpesaReceiptNumber);
}
