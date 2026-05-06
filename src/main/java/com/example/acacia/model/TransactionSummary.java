package com.example.acacia.model;

import com.example.acacia.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "all_transactions")
@Getter
public class TransactionSummary {
    @Id
    private String uniqueId;

    private Long originalId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private LocalDateTime createdAt;
    private String category;
    private String reference;
    private String description;
    private Long loanId;

    private Long memberId;
    private String memberName;
}
