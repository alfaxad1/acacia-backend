package com.example.acacia.service;

import com.example.acacia.dto.DashboardSummary;
import com.example.acacia.enums.*;
import com.example.acacia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ContributionRepository contributionRepository;
    private final LoanRepository loanRepository;
    private final ExtraRepository  extraRepository;
    private final MemberRepository memberRepository;
    private final FineRepository fineRepository;
    private final AccountAdjustmentRepository adjustmentRepository;

    public DashboardSummary getDashboardSummary() {

        BigDecimal totalContributions = contributionRepository.getSaccoBalance();
        BigDecimal totalPaidLoans = loanRepository.sumLoans(LoanStatus.REPAID);
        BigDecimal totalPaidFines = fineRepository.sumTotalPaidFines(FineStatus.PAID);
        BigDecimal totalDebits = adjustmentRepository.sumAdjustments(AdjustmentType.DEBIT);
        BigDecimal totalSurpluses = extraRepository.sumSurpluses(ExtraType.SURPLUS);

        BigDecimal totalActiveLoans = loanRepository.sumLoans(LoanStatus.DISBURSED);
        BigDecimal totalCredits = adjustmentRepository.sumAdjustments(AdjustmentType.CREDIT);

        BigDecimal inAccount = totalContributions.add(totalPaidLoans).add(totalPaidFines).add(totalDebits).add(totalSurpluses);
        BigDecimal outOfAccount = totalActiveLoans.add(totalCredits);

        BigDecimal balance = inAccount.subtract(outOfAccount);

        long activeLoans = loanRepository.countActive(LoanStatus.DISBURSED);
        long arrears = extraRepository.countMembersWithArrears(ExtraStatus.ACTIVE, ExtraType.ARREAR);

        long expected = memberRepository.countActive(MemberStatus.ACTIVE);
        long paid = contributionRepository.countPaidThisWeek();

        double compliance = expected == 0 ? 0 : (paid * 100.0) / expected;

        return new DashboardSummary(
                balance, totalActiveLoans, activeLoans, arrears, compliance
        );
    }

}
