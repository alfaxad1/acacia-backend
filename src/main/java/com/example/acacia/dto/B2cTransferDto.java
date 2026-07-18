package com.example.acacia.dto;

import com.example.acacia.enums.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class B2cTransferDto {
    private Long id;
    private BigDecimal amount;
    private String recipientPhone;
    private String reason;
    private TransactionStatus status;
    private Long initiatedById;
    private String initiatedByName;
    private Long authorizedById;
    private String authorizedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
