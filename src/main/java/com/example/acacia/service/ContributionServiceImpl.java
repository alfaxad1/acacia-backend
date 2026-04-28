package com.example.acacia.service;

import com.example.acacia.Exception.BusinessException;
import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.*;
import com.example.acacia.enums.*;
import com.example.acacia.model.*;
import com.example.acacia.repository.*;
import com.example.acacia.utility.FormatPhone;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContributionServiceImpl implements ContributionService {
    private final ContributionPeriodRepository periodRepository;
    private final MemberRepository memberRepository;
    private final ExtraRepository extraRepository;
    private final FineRepository fineRepository;
    private final SaccoSetupRepository saccoSetupRepository;
    private final ContributionRepository contributionRepository;
    private final CreditScoreService creditScoreService;
    private final ContributionArrearRepository contributionArrearRepository;
    private final MpesaService mpesaService;
    private final TransactionRepository transactionRepository;
    private final FormatPhone formatPhone;

    public StkPushResponse initiateContribution(  Long memberId, Long periodId) throws IOException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member doesn't exist"));

        List<Fine> oldUnpaidFines = fineRepository.findByMemberAndStatusAndFineDateBefore(
                member, FineStatus.UNPAID, LocalDate.now().minusDays(30));

        if (!oldUnpaidFines.isEmpty()) {
            long count = oldUnpaidFines.size();
            String message = count == 1
                    ? "The member has 1 unpaid fine older than 30 days. Please settle it first."
                    : "The member has " + count + " unpaid fines older than 30 days. Please settle them first.";

            throw new BusinessException(message, "UNPAID_FINES_PENDING");
        }

        ContributionPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period doesn't exist"));

        if (contributionRepository.existsByMemberAndPeriod(member, period)) {
            throw new IllegalStateException(
                    "Contribution already recorded for this period");
        }

        SaccoSetups setups = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);
        BigDecimal amount = setups.getContributionAmount();

        log.info("====Attempting stk push====");
        // 2. Trigger STK Push via MpesaService
        StkPushResponse mpesaResponse = mpesaService.stkPush(
                formatPhone.formatPhoneNumber(member.getPhone()),
                amount.toString(),
                "CONTRIBUTION-" + member.getMemberNumber(),
                "Weekly Contribution"
        );

        // 3. Save a "PENDING" transaction record
        // This links the M-Pesa request to your internal Member ID
        Transaction txn = new Transaction();
        txn.setCheckoutRequestID(mpesaResponse.getCheckoutRequestID());
        txn.setMember(member);
        txn.setAmount(amount);
        txn.setType(TransactionType.CONTRIBUTION);
        txn.setPeriod(period);
        txn.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(txn);

        return mpesaResponse;
    }

    @Override
    @Transactional
    public void addContribution(Long periodId, Long memberId, LocalDateTime paymentDate, BigDecimal amount) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member doesn't exist"));

        ContributionPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Period doesn't exist"));


        ContributionArrear contributionArrear = contributionArrearRepository.findByMemberAndPeriodAndIsPaid(member,
                period, false);

        if (contributionArrear != null) {
            contributionArrear.setPaid(true);
            contributionArrearRepository.save(contributionArrear);
        }

        SaccoSetups setups = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);

        BigDecimal amountToRecord = amount;
        if (amountToRecord == null || amountToRecord.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid contribution amount");
        }

        // Settle unpaid fines that are past 30 days starting with the oldest
//        List<Fine> fines = fineRepository.findByMemberAndStatusAndFineDateBeforeOrderByFineDateAsc(
//                member,
//                FineStatus.UNPAID,
//                LocalDate.now().minusDays(30));

//        for (Fine fine : fines) {
//
//            if (amountToRecord.compareTo(BigDecimal.ZERO) <= 0)
//                break;
//
//            BigDecimal fineAmount = fine.getAmount();
//
//            // Contribution can clear this fine fully
//            if (amountToRecord.compareTo(fineAmount) >= 0) {
//                amountToRecord = amountToRecord.subtract(fineAmount);
//                fine.setPaid(true);
//                fine.setStatus(FineStatus.PAID);
//                fineRepository.save(fine);
//            } else {
//                fine.setAmount(fineAmount.subtract(amountToRecord));
//                amountToRecord = BigDecimal.ZERO;
//                fineRepository.save(fine);
//            }
//        }

        // check if contribution is late and is not recorded
        boolean isLate = paymentDate
                .isAfter(period.getDeadline());
        boolean isRecorded = fineRepository.existsByMemberAndTypeAndReferenceId(member, FineTyp.LATE_PAYMENT, period.getId());

        if (isLate && !isRecorded) {
            Fine lateFine = Fine.builder()
                    .member(member)
                    .amount(setups.getLatePaymentFineAmount())
                    .status(FineStatus.UNPAID)
                    .type(FineTyp.LATE_PAYMENT)
                    .fineDate(LocalDate.now())
                    .referenceId(period.getId())
                    .build();
            fineRepository.save(lateFine);
            log.info("Late payment fine recorded for member {} on period {}", member.getId(), period.getId());
        }

        /**
         * Record contribution if contribution amount equal the required contribution amount
         * Record surplus if contribution amount is larger than the required contribution amount
         * Record arrear if contribution amount is less than the required contribution amount
         */
        BigDecimal requiredAmount = setups.getContributionAmount();

        Contribution savedContribution = new Contribution();
        savedContribution.setMember(member);
        savedContribution.setPeriod(period);
        savedContribution.setPaymentDate(paymentDate);
        savedContribution.setLate(isLate);

        // EXACT PAYMENT
        if (amountToRecord.compareTo(requiredAmount) == 0) {
            savedContribution.setAmount(requiredAmount);
            contributionRepository.save(savedContribution);
            // equityService.recalculateAllEquity();
            creditScoreService.updateCreditScore(member);

        }

        // OVERPAYMENT (SURPLUS)
        else if (amountToRecord.compareTo(requiredAmount) > 0) {

            savedContribution.setAmount(requiredAmount);
            contributionRepository.save(savedContribution);
            // equityService.recalculateAllEquity();
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
            // equityService.recalculateAllEquity();
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
                        member, ExtraType.ARREAR, ExtraStatus.ACTIVE);

        List<Extra> surpluses = extraRepository
                .findByMemberAndTypeAndStatusOrderByIdAsc(
                        member, ExtraType.SURPLUS, ExtraStatus.ACTIVE);

        for (Extra surplus : surpluses) {

            BigDecimal surplusAmount = surplus.getAmount();

            for (Extra arrear : arrears) {

                if (surplusAmount.compareTo(BigDecimal.ZERO) <= 0)
                    break;

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
                surplus.setStatus(ExtraStatus.SETTLED);
            }
            extraRepository.save(surplus);
        }
    }

    public List<ContributionResponseDTO> getAllContributions(Pageable pageable) {
        return contributionRepository.findAllFlattened(pageable);
    }

    public ContributionResponseDTO getContributionById(Long id) {
        return contributionRepository.findById(id)
                .map(c -> new ContributionResponseDTO(
                        c.getId(),
                        c.getMember().getId(),
                        c.getMember().getFullName(),
                        c.getPeriod().getDate(),
                        c.getAmount(),
                        c.getPaymentDate(),
                        c.isLate()))
                .orElseThrow(() -> new EntityNotFoundException("Contribution not found"));
    }

    @Override
    public Response<List<ContributionArrearDto>> getArrears(Pageable pageable) {
        try {
            boolean isPaid = false;
            Page<Tuple> arrearsPage = contributionArrearRepository.findArrears(pageable, isPaid);

            List<ContributionArrearDto> dtoList = arrearsPage.getContent().stream()
                    .map(tuple -> ContributionArrearDto.builder()
                            .id(tuple.get(0, Long.class))
                            .arrearAmount(tuple.get(1, BigDecimal.class))
                            .memberName(tuple.get(2, String.class))
                            .periodDate(tuple.get(3, LocalDate.class))
                            .fineAmount(tuple.get(4, BigDecimal.class))
                            .build())
                    .collect(Collectors.toList());

            MetaData metaData = MetaData.builder()
                    .page(arrearsPage.getNumber())
                    .totalElements(arrearsPage.getTotalElements())
                    .totalPages(arrearsPage.getTotalPages())
                    .limit(arrearsPage.getSize())
                    .build();

            return Response.<List<ContributionArrearDto>>builder()
                    .status(ResponseStatusEnum.SUCCESS)
                    .data(dtoList)
                    .message("Contribution arrears retrieved successfully")
                    .metaData(metaData)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch arrears: " + e.getMessage());
        }
    }

}
