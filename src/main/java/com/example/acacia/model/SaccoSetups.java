package com.example.acacia.model;

import com.example.acacia.enums.SetupStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sacco_setups")
@Entity
public class SaccoSetups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek contributionDay;

    private BigDecimal contributionAmount;

    private Long daysToDeadline;

    private LocalTime deadlineTime;

    private BigDecimal latePaymentFineAmount;

    private BigDecimal loanInterestRate;

    private BigDecimal loanPenaltyRate;

    private int loanDuration;

    @Enumerated(EnumType.STRING)
    private SetupStatus status;
}
