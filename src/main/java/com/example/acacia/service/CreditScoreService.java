package com.example.acacia.service;

import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.enums.FineStatus;
import com.example.acacia.enums.LoanStatus;
import com.example.acacia.model.CreditScore;
import com.example.acacia.model.Member;
import com.example.acacia.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final ContributionRepository contributionRepository;
    private final FineRepository fineRepository;
    private final LoanRepository loanRepository;
    private final ExtraRepository extraRepository;
    private final CreditScoreRepository creditScoreRepository;

    @Transactional
    public void updateCreditScore(Member member) {

        int score = 100;

        score -= contributionRepository.countLate(member) * 5;
        score -= fineRepository.countUnpaid(member, FineStatus.UNPAID) * 10;
        score -= loanRepository.countDefaults(member, LoanStatus.DEFAULTED) * 30;
        score -= extraRepository.countActiveArrears(member, ExtraType.ARREAR, ExtraStatus.ACTIVE) * 5;

        score = Math.max(score, 0);

        CreditScore creditScore = creditScoreRepository
                .findById(member.getId())
                .orElse(
                        CreditScore.builder()
                                .member(member)
                                .score(0)
                                .lastUpdated(LocalDate.now())
                                .build()
                );

        creditScore.setScore(score);
        creditScore.setLastUpdated(LocalDate.now());

        creditScoreRepository.save(creditScore);
    }


}

