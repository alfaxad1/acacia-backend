package com.example.acacia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "fine_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FineType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    // Fixed fine (e.g. 50 KES)
    private BigDecimal amount;

    // Percentage fine (e.g. 5%)
    private BigDecimal percentage;
}
