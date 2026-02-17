package com.example.acacia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "contribution_arrears")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContributionArrear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "period_id")
    private ContributionPeriod period;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "fine_id")
    private Fine fine;

}
