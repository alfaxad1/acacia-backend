package com.example.acacia.dto;

import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
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
public class ExtraDto {
    private Long id;
    private String memberName;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate periodDate;
    private ExtraType extraType;
    private ExtraStatus status;
}
