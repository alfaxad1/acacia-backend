package com.example.acacia.service;

import com.example.acacia.dto.LoanEligibilityResult;
import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.model.Member;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.CreditScoreRepository;
import com.example.acacia.repository.ExtraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LoanEligibilityService {

    private final ContributionRepository contributionRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final ExtraRepository extraRepository;

    public LoanEligibilityResult checkEligibility(
            Member member,
            BigDecimal requestedAmount
    ) {

        BigDecimal saccoBalance = contributionRepository.getSaccoBalance();
        BigDecimal maxLoanPool = saccoBalance.multiply(BigDecimal.valueOf(0.5));

        int creditScore = creditScoreRepository
                .findById(member.getId())
                .orElseThrow()
                .getScore();

        // ❌ Member has arrears
        boolean hasArrears = extraRepository
                .existsByMemberAndTypeAndStatus(
                        member, ExtraType.ARREAR, ExtraStatus.ACTIVE
                );

        if (hasArrears) {
            return new LoanEligibilityResult(
                    false,
                    BigDecimal.ZERO,
                    "Member has active arrears"
            );
        }

        BigDecimal creditLimit;

        if (creditScore >= 80) {
            creditLimit = maxLoanPool;
        } else if (creditScore >= 60) {
            creditLimit = maxLoanPool.multiply(BigDecimal.valueOf(0.7));
        } else if (creditScore >= 40) {
            creditLimit = maxLoanPool.multiply(BigDecimal.valueOf(0.4));
        } else {
            return new LoanEligibilityResult(
                    false,
                    BigDecimal.ZERO,
                    "Credit score too low"
            );
        }

        BigDecimal eligibleAmount = requestedAmount.min(creditLimit);

        return new LoanEligibilityResult(
                true,
                eligibleAmount,
                "Eligible"
        );
    }
}
