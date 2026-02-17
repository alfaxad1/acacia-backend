package com.example.acacia.model;

import com.example.acacia.enums.VoteDecision;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class LoanVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Loan loan;

    @ManyToOne
    private Member committeeMember;

    @Enumerated(EnumType.STRING)
    private VoteDecision decision; // APPROVE / REJECT

    private LocalDateTime votedAt;
}

