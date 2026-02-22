package com.example.acacia.model;

import com.example.acacia.enums.FineStatus;
import com.example.acacia.enums.FineTyp;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "fine_type_id")
//    private FineType fineType;

    @Enumerated(EnumType.STRING)
    private FineTyp type;

    @Enumerated(EnumType.STRING)
    private FineStatus status = FineStatus.UNPAID;

    // Can reference loan_id or contribution_id
    private Long referenceId;

    private BigDecimal amount;
    private LocalDate fineDate;
    private boolean isPaid = false;
    private LocalDate paidDate;
}
