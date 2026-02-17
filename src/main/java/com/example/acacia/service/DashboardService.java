package com.example.acacia.service;

import com.example.acacia.dto.DashboardSummary;
import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.enums.LoanStatus;
import com.example.acacia.enums.MemberStatus;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.ExtraRepository;
import com.example.acacia.repository.LoanRepository;
import com.example.acacia.repository.MemberRepository;
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

    public DashboardSummary getDashboardSummary() {

        BigDecimal balance = contributionRepository.getSaccoBalance();
        BigDecimal loans = loanRepository.sumLoansIssued(LoanStatus.DISBURSED);
        long activeLoans = loanRepository.countActive(LoanStatus.DISBURSED);
        long arrears = extraRepository.countMembersWithArrears(ExtraStatus.ACTIVE, ExtraType.ARREAR);

        long expected = memberRepository.countActive(MemberStatus.ACTIVE);
        long paid = contributionRepository.countPaidThisWeek();

        double compliance = expected == 0 ? 0 : (paid * 100.0) / expected;

        return new DashboardSummary(
                balance, loans, activeLoans, arrears, compliance
        );
    }

}
