package com.example.acacia.model;

import com.example.acacia.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private BigDecimal requestedAmount;

    private BigDecimal approvedAmount;

    private BigDecimal eligibleAmount;

    private BigDecimal interestRate;

    private BigDecimal penaltyRate;

    private BigDecimal interestAmount;

    private BigDecimal totalPayable;

    private LocalDate requestDate;

    private int durationDays;

    private LocalDate dueDate;

    private LocalDate approvalDate;

    private LocalDate disbursementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Member approver;

    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private LocalDate repaidDate;
}
