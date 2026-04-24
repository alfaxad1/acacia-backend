package com.example.acacia.model;

import com.example.acacia.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "b2c_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class B2cTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(unique = true)
    private String conversationId;

    @Column(unique = true)
    private String originatorConversationId;

    @Column(unique = true)
    private String transactionId;

    private BigDecimal amount;

    private BigDecimal fee;

    private String recipientPhone;

    private String errorReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = (status == null) ? TransactionStatus.PENDING : status;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
