package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardSummary {

    private BigDecimal saccoBalance;
    private BigDecimal totalLoansIssued;
    private long activeLoans;
    private long membersWithArrears;
    private double weeklyComplianceRate;
}

