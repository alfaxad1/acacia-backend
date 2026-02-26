package com.example.acacia.dto;

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
public class ContributionArrearDto {
    private Long id;
    private LocalDate periodDate;
    private String memberName;
    private BigDecimal arrearAmount;
    private BigDecimal fineAmount;
}
