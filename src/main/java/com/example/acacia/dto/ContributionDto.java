package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionDto {
    private Long id;
    private String memberName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private boolean isLate;
}
