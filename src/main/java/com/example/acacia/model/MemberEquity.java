package com.example.acacia.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "member_equities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEquity {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalContribution;

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal equityPercentage;
}
