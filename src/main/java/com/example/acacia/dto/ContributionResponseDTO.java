package com.example.acacia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContributionResponseDTO(
        Long id,
        String memberName,
        LocalDate periodDate,
        BigDecimal amount,
        LocalDateTime paymentDate,
        boolean isLate
) {}
