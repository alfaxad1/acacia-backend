package com.example.acacia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FineTypeRequest {
    private String name;
    private String description;
    private BigDecimal amount;
    private BigDecimal percentage;
}
