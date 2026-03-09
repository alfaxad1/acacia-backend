package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PersonalStats {
    private BigDecimal totalFinesAmount;
    private Long numberOfFines;
    private BigDecimal totalLoanAmount;
    private Long numberOfLoans;
    private BigDecimal missedContributionsAmount;
    private Long numberOfMissedContributions;
}
