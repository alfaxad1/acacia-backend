package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanEligibilityResult {

    private boolean eligible;
    private BigDecimal eligibleAmount;
    private String reason;
}

