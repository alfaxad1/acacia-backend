package com.example.acacia.repository;

import com.example.acacia.model.B2cTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface B2cTransactionsRepository extends JpaRepository<B2cTransactions, Long> {
    B2cTransactions findByConversationId(String conversationId);
}
