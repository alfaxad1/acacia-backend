package com.example.acacia.dto;

import com.example.acacia.enums.LoanStatus;
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
public class LoanDto {
    private Long id;
    private String memberName;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private BigDecimal interestAmount;
    private LoanStatus status;
    private Integer duration;
    private LocalDate requestDate;
    private LocalDate approvedDate;
    private String memberNo;
    private Long memberId;

}
