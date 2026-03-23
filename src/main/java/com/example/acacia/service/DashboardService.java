package com.example.acacia.service;

import com.example.acacia.dto.DashboardSummary;
import com.example.acacia.dto.PersonalStats;
import com.example.acacia.enums.*;
import com.example.acacia.model.Contribution;
import com.example.acacia.model.Member;
import com.example.acacia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ContributionRepository contributionRepository;
    private final ContributionArrearRepository contributionArrearRepository;
    private final LoanRepository loanRepository;
    private final ExtraRepository  extraRepository;
    private final MemberRepository memberRepository;
    private final FineRepository fineRepository;
    private final AccountAdjustmentRepository adjustmentRepository;

    public DashboardSummary getDashboardSummary(Long userId) {
        BigDecimal totalContributions = contributionRepository.getSaccoBalance();
        BigDecimal totalPaidLoans = loanRepository.sumPaidLoans(List.of(LoanStatus.REPAID));
        BigDecimal totalPaidFines = fineRepository.sumTotalPaidFines(FineStatus.PAID);
        BigDecimal totalDebits = adjustmentRepository.sumAdjustments(AdjustmentType.DEBIT);
        BigDecimal totalSurpluses = extraRepository.sumSurpluses(ExtraType.SURPLUS);

        BigDecimal totalActiveLoans = loanRepository.sumLoans(List.of(LoanStatus.DISBURSED, LoanStatus.DEFAULTED, LoanStatus.REPAID));
        BigDecimal totalCredits = adjustmentRepository.sumAdjustments(AdjustmentType.CREDIT);

        BigDecimal inAccount = totalContributions.add(totalPaidLoans).add(totalPaidFines).add(totalDebits).add(totalSurpluses);
        BigDecimal outOfAccount = totalActiveLoans.add(totalCredits);

        BigDecimal balance = inAccount.subtract(outOfAccount);

        long activeLoans = loanRepository.countActive(LoanStatus.DISBURSED);

        Long members = memberRepository.countActive(MemberStatus.ACTIVE);
        Member member = memberRepository.findById(userId).orElseThrow(() -> new RuntimeException("Member Not Found"));

        List<Contribution> memberContributions = contributionRepository.findAllByMember(member);
        BigDecimal totalMemberContribution = memberContributions.stream()
                .map(Contribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long numberOfFines = null;
        BigDecimal totalFinesAmount = null;
        Long numberOfLoans = null;
        BigDecimal totalLoansAmount = null;
        Long totalArrears = null;
        BigDecimal totalArrearsAmount = null;

        List<Object[]> finesInfo = fineRepository.getMemberFines(userId, FineStatus.UNPAID);

        if (finesInfo != null && !finesInfo.isEmpty()) {
            Object[] row = finesInfo.get(0);

            numberOfFines = ((Number) row[0]).longValue();
            totalFinesAmount = (BigDecimal) row[1];
        }

        List<Object[]> loansInfo = loanRepository.getMemberLoansInfo(userId, List.of(LoanStatus.DISBURSED, LoanStatus.DEFAULTED));
        if (loansInfo != null && !loansInfo.isEmpty()) {
            Object[] row = loansInfo.get(0);
            numberOfLoans = ((Number) row[0]).longValue();
            totalLoansAmount = (BigDecimal) row[1];
        }

        List<Object[]> arrearsResults = contributionArrearRepository.getMemberContributionArrear(userId);
        if (arrearsResults != null && !arrearsResults.isEmpty()) {
            Object[] row = arrearsResults.get(0);

            totalArrears = ((Number) row[0]).longValue();
            totalArrearsAmount = (BigDecimal) row[1];
        }

        return new DashboardSummary(
                balance,
                totalActiveLoans,
                activeLoans,
                balance.multiply(BigDecimal.valueOf(0.5)),
                totalContributions,
                members,
                new PersonalStats(
                        totalFinesAmount,
                        numberOfFines,
                        totalLoansAmount,
                        numberOfLoans,
                        totalArrearsAmount,
                        totalArrears,
                        member.isPremium(),
                        member.getJoinDate(),
                        totalMemberContribution
                )
        );
    }

}
