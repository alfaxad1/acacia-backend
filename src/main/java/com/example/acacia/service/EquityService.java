package com.example.acacia.service;

import com.example.acacia.enums.MemberStatus;
import com.example.acacia.model.Member;
import com.example.acacia.model.MemberEquity;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.LoanRepaymentRepository;
import com.example.acacia.repository.MemberEquityRepository;
import com.example.acacia.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquityService {

    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final MemberEquityRepository memberEquityRepository;

    @Transactional
    public void recalculateAllEquity() {
        // Fetch all active members
        List<Member> members = memberRepository.findAllByStatus(MemberStatus.ACTIVE);

        Map<Long, BigDecimal> totals = new HashMap<>();
        BigDecimal saccoTotal = BigDecimal.ZERO;

        // Calculate total contribution + interest per member
        for (Member m : members) {
            BigDecimal contrib = contributionRepository.sumContributions(m);
            BigDecimal interest = loanRepaymentRepository.sumLoanRepayments(m);

            BigDecimal total = contrib.add(interest);
            totals.put(m.getId(), total);
            saccoTotal = saccoTotal.add(total);
        }

        // Recalculate equity percentages
        for (Member m : members) {
            BigDecimal memberTotal = totals.get(m.getId());
            BigDecimal percentage = saccoTotal.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : memberTotal.divide(saccoTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // Fetch existing MemberEquity or create new
            MemberEquity equity = memberEquityRepository.findById(m.getId())
                    .orElseGet(() -> {
                        MemberEquity me = new MemberEquity();
                        // Always assign a managed member reference
                        Member managedMember = memberRepository.getReferenceById(m.getId());
                        me.setMember(managedMember);
                        return me;
                    });

            // Update totals and percentage
            equity.setTotalContribution(memberTotal);
            equity.setEquityPercentage(percentage);

            // Save
            memberEquityRepository.save(equity);
        }
    }


    @Scheduled(cron = "0 0 2 1 * ?") // 1st day monthly
    public void monthlyEquityRecalc() {
        recalculateAllEquity();
    }
}

