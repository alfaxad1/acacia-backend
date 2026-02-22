package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionPeriodDto {
    private Long id;
    private LocalDate date;
    private LocalDateTime deadline;
    private BigDecimal amountRequired;
    private List<ContributionDto> contributions;
}
