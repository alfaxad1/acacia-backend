package com.example.acacia.model;

import com.example.acacia.enums.AdjustmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "account_adjustments")
public class AccountAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private BigDecimal transactionCost;
    private BigDecimal totalCost;
    private LocalDate date;
    private AdjustmentType type;
    private String description;
}
