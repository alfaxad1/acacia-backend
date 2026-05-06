package com.example.acacia.dto;

import com.example.acacia.model.TransactionSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardSummary {
    private BigDecimal saccoBalance;
    private BigDecimal paybillBalance;
    private BigDecimal totalLoansIssued;
    private long activeLoans;
    private BigDecimal availableLoanAmount;
    private BigDecimal totalContributions;
    private Long members;
    private List<TransactionSummary> recentTransactions;
    private PersonalStats personalStats;
}

