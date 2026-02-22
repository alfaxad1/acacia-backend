package com.example.acacia.dto;

import com.example.acacia.enums.FineTyp;
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
public class FineRequest {
    private Long memberId;
    private FineTyp type;
    private BigDecimal amount;
    private LocalDate fineDate;
}
