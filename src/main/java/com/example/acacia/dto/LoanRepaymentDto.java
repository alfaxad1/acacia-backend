package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate paymentDate;
}
