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
public class ContributionRequest {
    private Long memberId;
    private Long periodId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
}
