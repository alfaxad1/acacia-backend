package com.example.acacia.dto;

import com.example.acacia.enums.FineStatus;

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
public class FineDto {
    private Long id;
    private String memberName;
    private Long memberId;
    private BigDecimal amount;
    private LocalDate  date;
    private FineStatus status;
    private Long fineTypeId;
    private String fineTypeName;
    private LocalDate paidDate;

}
