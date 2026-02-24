package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.LoanDto;
import com.example.acacia.dto.LoanEligibilityResult;
import com.example.acacia.enums.*;
import com.example.acacia.model.*;
import com.example.acacia.repository.*;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanService {
    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanEligibilityService eligibilityService;
    private final LoanRepository loanRepository;
    private final ExtraRepository extraRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanRepaymentScheduleRepository scheduleRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final EquityService equityService;
    private final CreditScoreService creditScoreService;
    private final SaccoSetupRepository saccoSetupRepository;
    private final EmailService emailService;

    @Transactional
    public void requestLoan(
            Long memberId,
            BigDecimal requestedAmount
    ) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        LoanEligibilityResult result =
                eligibilityService.checkEligibility(member, requestedAmount);

        if (!result.isEligible()) {
            throw new IllegalStateException(result.getReason());
        }

        SaccoSetups saccoSetups = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setRequestedAmount(requestedAmount);
        loan.setEligibleAmount(result.getEligibleAmount());
        loan.setInterestRate(saccoSetups.getLoanInterestRate());
        loan.setPenaltyRate(saccoSetups.getLoanPenaltyRate());
        loan.setDurationDays(saccoSetups.getLoanDuration());
        loan.setStatus(LoanStatus.PENDING);
        loan.setRequestDate(LocalDate.now());

        loanRepository.save(loan);

        String subject = "LOAN REQUEST BY " + member.getFullName().toUpperCase();
        String content = member.getFullName() + " is requesting for a loan of Ksh." +  requestedAmount + ". Please login into the system and place your vote";
        log.info("User email: {} ", member.getEmail());

        List<Member> members = memberRepository.findAllByStatus(MemberStatus.ACTIVE);

        for (Member member1 : members) {
            try {
                emailService.sendMail(member1.getEmail(), subject, content);
            } catch (Exception e) {
                log.error("Failed to send mail for member {} Error: {}", member1.getEmail(), e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional
    public void approveLoan(Long loanId) {

        //Member approver = memberRepository.findAById(approverId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Loan not pending");
        }

        BigDecimal approvedAmount = loan.getRequestedAmount().min(loan.getEligibleAmount());
        BigDecimal interest = approvedAmount.multiply(loan.getInterestRate()).divide(BigDecimal.valueOf(100));
        BigDecimal totalPayable = approvedAmount.add(interest);

        loan.setApprovedAmount(approvedAmount);
        loan.setInterestAmount(interest);
        loan.setTotalPayable(totalPayable);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        //loan.setApprover(approver);

        loanRepository.save(loan);
    }

    @Transactional
    public void disburseLoan(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException("Loan not approved");
        }

        BigDecimal saccoBalance = contributionRepository.getSaccoBalance();

        if (saccoBalance.compareTo(loan.getApprovedAmount()) < 0) {
            throw new IllegalStateException("Insufficient SACCO balance");
        }

        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDisbursementDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(loan.getDurationDays()));

        loanRepository.save(loan);

    }

    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1AM
    @Transactional
    public void applyLoanPenalties() {

        List<Loan> overdueLoans = loanRepository
                .findByStatusAndDueDateBefore(
                        LoanStatus.DISBURSED, LocalDate.now()
                );

        for (Loan loan : overdueLoans) {

            BigDecimal penalty =
                    loan.getApprovedAmount()
                            .multiply(loan.getPenaltyRate())
                            .divide(BigDecimal.valueOf(100));

            long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());

            LoanPenalty lp = new LoanPenalty();
            lp.setLoan(loan);
            lp.setDaysLate(Math.max(daysLate, 0));
            lp.setAmount(penalty);
            lp.setPenaltyDate(LocalDate.now());

            loanPenaltyRepository.save(lp);

            loan.setStatus(LoanStatus.DEFAULTED);
            loanRepository.save(loan);
        }
    }

    @Transactional
    public void repayLoan(Long loanId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.DISBURSED
                && loan.getStatus() != LoanStatus.DEFAULTED) {
            throw new IllegalStateException("Loan is not active");
        }

        List<LoanRepayment> repayments =
                loanRepaymentRepository.findByLoan(loan);

        BigDecimal repaidAmount = repayments.stream()
                .map(LoanRepayment::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = loan.getTotalPayable().subtract(repaidAmount);

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Loan already fully repaid");
        }

        BigDecimal amountToApply = amount.min(balance);
        BigDecimal surplus = amount.subtract(amountToApply);

        // Record repayment
        LoanRepayment repayment = LoanRepayment.builder()
                .loan(loan)
                .paymentDate(LocalDate.now())
                .amount(amountToApply)
                .build();

        loanRepaymentRepository.save(repayment);

        // Handle surplus
        if (surplus.compareTo(BigDecimal.ZERO) > 0) {
            Extra extras = Extra.builder()
                    .member(loan.getMember())
                    .amount(surplus)
                    .type(ExtraType.SURPLUS)
                    .status(ExtraStatus.ACTIVE)
                    .recordedDate(LocalDate.now())
                    .build();

            extraRepository.save(extras);
        }

        if (amountToApply.compareTo(balance) == 0) {
            loan.setStatus(LoanStatus.REPAID);
            loan.setRepaidDate(LocalDate.now());
            loanRepository.save(loan);
        }

        //equityService.recalculateAllEquity();
        creditScoreService.updateCreditScore(loan.getMember());

    }


    @Transactional
    public void rejectLoan(Long loanId, String reason) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(reason);

        loanRepository.save(loan);
    }

    public List<LoanDto> getLoans(LoanStatus loanStatus) {
        try {
            List<LoanDto> dtoList = new ArrayList<>();
            List<Tuple> loans = loanRepository.getActiveLoans(loanStatus);
            for (Tuple loan : loans) {
                LoanDto dto = LoanDto.builder()
                        .id(loan.get(0, Long.class))
                        .memberName(loan.get(1, String.class))
                        .requestedAmount(loan.get(2, BigDecimal.class))
                        .approvedAmount(loan.get(3, BigDecimal.class))
                        .paidAmount(loan.get(4, BigDecimal.class))
                        .dueDate(loan.get(5, LocalDate.class))
                        .interestAmount(loan.get(6, BigDecimal.class))
                        .status(loan.get(7, LoanStatus.class))
                        .duration(loan.get(8, Integer.class))
                        .requestDate(loan.get(9, LocalDate.class))
                        .approvedDate(loan.get(10, LocalDate.class))
                        .memberNo(loan.get(11, String.class))
                        .memberId(loan.get(12, Long.class))
                        .build();
                dtoList.add(dto);
            }
            return dtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
