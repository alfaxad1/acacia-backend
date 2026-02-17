package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.ContributionRequest;
import com.example.acacia.enums.*;
import com.example.acacia.model.*;
import com.example.acacia.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final ContributionPeriodRepository periodRepository;
    private final MemberRepository memberRepository;
    private final ExtraRepository extraRepository;
    private final FineRepository fineRepository;
    private final SaccoSetupRepository saccoSetupRepository;
    private final ContributionRepository contributionRepository;
    private final EquityService equityService;
    private final CreditScoreService creditScoreService;

    @Override
    @Transactional
    public void addContribution(ContributionRequest contribution) {

        // 1. Validate member & period
        Member member = memberRepository.findById(contribution.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member doesn't exist"));

        ContributionPeriod period = periodRepository.findById(contribution.getPeriodId())
                .orElseThrow(() -> new ResourceNotFoundException("Period doesn't exist"));

        if (contributionRepository.existsByMemberAndPeriod(member, period)) {
            throw new IllegalStateException(
                    "Contribution already recorded for this period"
            );
        }

        SaccoSetups setups = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);

        BigDecimal amountToRecord = contribution.getAmount();
        if (amountToRecord == null || amountToRecord.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid contribution amount");
        }

    /* =========================================================
       2. SETTLE UNPAID FINES (OLDEST FIRST)
       ========================================================= */
        List<Fine> fines = fineRepository.findByMemberAndStatusOrderByIdAsc(member, FineStatus.UNPAID);

        for (Fine fine : fines) {

            if (amountToRecord.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal fineAmount = fine.getAmount();

            // Contribution can clear this fine fully
            if (amountToRecord.compareTo(fineAmount) >= 0) {
                amountToRecord = amountToRecord.subtract(fineAmount);
                fine.setStatus(FineStatus.PAID);
                fineRepository.save(fine);
            }
            // Contribution can clear this fine partially
            else {
                fine.setAmount(fineAmount.subtract(amountToRecord));
                amountToRecord = BigDecimal.ZERO;
                fineRepository.save(fine);
            }
        }

    /* =========================================================
       3. CHECK IF CONTRIBUTION IS LATE
       ========================================================= */
        boolean isLate = contribution.getPaymentDate()
                .isAfter(period.getDeadline());

//        if (isLate) {
//            Fine lateFine = Fine.builder()
//                    .member(member)
//                    .amount(setups.getLatePaymentFineAmount())
//                    .status(FineStatus.UNPAID)
//                    .type(FineTyp.LATE_PAYMENT)
//                    .fineDate(LocalDate.now())
//                    .referenceId(period.getId())
//                    .build();
//            fineRepository.save(lateFine);
//        }

    /* =========================================================
       4. RECORD CONTRIBUTION / SURPLUS / ARREARS
       ========================================================= */
        BigDecimal requiredAmount = setups.getContributionAmount();

        Contribution savedContribution = new Contribution();
        savedContribution.setMember(member);
        savedContribution.setPeriod(period);
        savedContribution.setPaymentDate(contribution.getPaymentDate());
        savedContribution.setLate(isLate);

        // EXACT PAYMENT
        if (amountToRecord.compareTo(requiredAmount) == 0) {
            savedContribution.setAmount(requiredAmount);
            contributionRepository.save(savedContribution);
            //equityService.recalculateAllEquity();
            creditScoreService.updateCreditScore(member);

        }

        // OVERPAYMENT (SURPLUS)
        else if (amountToRecord.compareTo(requiredAmount) > 0) {

            savedContribution.setAmount(requiredAmount);
            contributionRepository.save(savedContribution);
            //equityService.recalculateAllEquity();
            creditScoreService.updateCreditScore(member);

            BigDecimal surplusAmount = amountToRecord.subtract(requiredAmount);

            Extra surplus = Extra.builder()
                    .member(member)
                    .period(period)
                    .amount(surplusAmount)
                    .type(ExtraType.SURPLUS)
                    .status(ExtraStatus.ACTIVE)
                    .recordedDate(LocalDate.now())
                    .build();

            extraRepository.save(surplus);
        }

        // UNDERPAYMENT (ARREAR)
        else if (amountToRecord.compareTo(requiredAmount) < 0) {

            savedContribution.setAmount(amountToRecord);
            contributionRepository.save(savedContribution);
            //equityService.recalculateAllEquity();
            creditScoreService.updateCreditScore(member);

            BigDecimal arrearAmount = requiredAmount.subtract(amountToRecord);

            Extra arrear = Extra.builder()
                    .member(member)
                    .period(period)
                    .amount(arrearAmount)
                    .type(ExtraType.ARREAR)
                    .status(ExtraStatus.ACTIVE)
                    .recordedDate(LocalDate.now())
                    .build();

            extraRepository.save(arrear);
        }

        clearArrearsWithSurplus(member);
    }

    @Transactional
    public void clearArrearsWithSurplus(Member member) {

        List<Extra> arrears = extraRepository
                .findByMemberAndTypeAndStatusOrderByIdAsc(
                        member, ExtraType.ARREAR, ExtraStatus.ACTIVE
                );

        List<Extra> surpluses = extraRepository
                .findByMemberAndTypeAndStatusOrderByIdAsc(
                        member, ExtraType.SURPLUS, ExtraStatus.ACTIVE
                );

        for (Extra surplus : surpluses) {

            BigDecimal surplusAmount = surplus.getAmount();

            for (Extra arrear : arrears) {

                if (surplusAmount.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal arrearAmount = arrear.getAmount();

                // Surplus clears arrear fully
                if (surplusAmount.compareTo(arrearAmount) >= 0) {
                    surplusAmount = surplusAmount.subtract(arrearAmount);
                    arrear.setStatus(ExtraStatus.SETTLED);
                    arrear.setAmount(BigDecimal.ZERO);
                    extraRepository.save(arrear);
                }
                // Partial clear
                else {
                    arrear.setAmount(arrearAmount.subtract(surplusAmount));
                    surplusAmount = BigDecimal.ZERO;
                    extraRepository.save(arrear);
                }
            }

            surplus.setAmount(surplusAmount);
            if (surplusAmount.compareTo(BigDecimal.ZERO) == 0) {
                surplus.setStatus(ExtraStatus.ACTIVE);
            }
            extraRepository.save(surplus);
        }
    }


}
