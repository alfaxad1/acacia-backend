package com.example.acacia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class SaccoWallet {
    @Id
    private Long id = 1L;
    private BigDecimal mpesaFloatBalance = BigDecimal.ZERO;
    private LocalDateTime lastUpdated;
}