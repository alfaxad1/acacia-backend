package com.example.acacia.model;

import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Relationship ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private ContributionPeriod period;

    // --- Financial Details ---
    @Column(nullable = false)
    private BigDecimal amount;

    private String description;

    // --- M-Pesa Specific Tracking ---

    @Column(unique = true)
    private String checkoutRequestID; // Used for STK Push tracking

    @Column(unique = true)
    private String merchantRequestID; // Secondary ID from Safaricom

    @Column(unique = true)
    private String mpesaReceiptNumber; // e.g., RKT1234567 (Filled after success)

    // --- Status and Metadata ---

    @Enumerated(EnumType.STRING)
    private TransactionStatus status; // PENDING, COMPLETED, FAILED, CANCELLED

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String phoneNumber; // The number that made the payment

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