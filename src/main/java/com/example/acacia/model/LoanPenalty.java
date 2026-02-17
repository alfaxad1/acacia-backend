package com.example.acacia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_penalties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanPenalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private Long daysLate;
    private BigDecimal amount;
    private LocalDate penaltyDate;
    private boolean isPaid = false;
}
