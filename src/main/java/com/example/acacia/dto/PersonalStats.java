package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PersonalStats {
    private BigDecimal totalFinesAmount;
    private Long numberOfFines;
    private BigDecimal totalLoanAmount;
    private Long numberOfLoans;
    private BigDecimal missedContributionsAmount;
    private Long numberOfMissedContributions;
    private boolean isPremium;
    private LocalDate joinDate;
    private BigDecimal totalMemberContribution;
}
