package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.dto.LoanDto;
import com.example.acacia.dto.LoanEligibilityResult;
import com.example.acacia.dto.StkPushResponse;
import com.example.acacia.enums.*;
import com.example.acacia.model.*;
import com.example.acacia.repository.*;
import com.example.acacia.utility.FormatPhone;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final LoanEligibilityService eligibilityService;
    private final LoanRepository loanRepository;
    private final ExtraRepository extraRepository;
    private final LoanPenaltyRepository loanPenaltyRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final CreditScoreService creditScoreService;
    private final SaccoSetupRepository saccoSetupRepository;
    private final EmailService emailService;
    private final MpesaService mpesaService;
    private final FormatPhone formatPhone;
    private final TransactionRepository transactionRepository;
    private final SaccoWalletRepository walletRepository;

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
    public void approveAndDisburse(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Loan is not pending");
        }

        // 1. Calculate Loan Details
        BigDecimal approvedAmount = loan.getRequestedAmount().min(loan.getEligibleAmount());
        // ... (your existing interest calculation logic) ...

        // 2. CHECK WALLET BALANCE (Pre-check using our stored float)
        SaccoWallet wallet = walletRepository.findById(1L).orElse(new SaccoWallet());
        if (wallet.getMpesaFloatBalance().compareTo(approvedAmount) < 0) {
            throw new IllegalStateException("Insufficient M-Pesa Float to disburse this loan.");
        }

        // 3. Update Loan State
        loan.setApprovedAmount(approvedAmount);
        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovalDate(LocalDate.now());
        loanRepository.save(loan);

        // 4. Trigger B2C Payment
        try {
            mpesaService.disburseFunds(loan.getMember().getPhone(), approvedAmount, loan.getId().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate M-Pesa disbursement: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1AM
    @Transactional
    public void processLoanNotificationsAndPenalties() {
        LocalDate today = LocalDate.now();

        List<Loan> overdueLoans = loanRepository.findByStatusInAndDueDateBefore(
                List.of(LoanStatus.DISBURSED, LoanStatus.DEFAULTED), today);

        for (Loan loan : overdueLoans) {
            if (shouldApplyPenalty(loan, today)) {
                applyPenaltyAndMarkDefault(loan, today);
            }
        }

        LocalDate sevenDaysFromNow = today.plusDays(7);
        List<Loan> upcomingSevenDays = loanRepository.findByStatusAndDueDate(LoanStatus.DISBURSED, sevenDaysFromNow);
        for (Loan loan : upcomingSevenDays) {
            sendReminder(loan, "Loan Repayment Reminder: 7 Days Remaining");
        }

        for (int i = 1; i <= 3; i++) {
            LocalDate targetDate = today.plusDays(i);
            List<Loan> finalReminders = loanRepository.findByStatusAndDueDate(LoanStatus.DISBURSED, targetDate);
            for (Loan loan : finalReminders) {
                sendReminder(loan, "URGENT: Loan Repayment Due in " + i + " Days");
            }
        }
    }

    private boolean shouldApplyPenalty(Loan loan, LocalDate today) {
        if (loan.getStatus() == LoanStatus.DISBURSED) {
            return true;
        }

        long daysOverdue = ChronoUnit.DAYS.between(loan.getDueDate(), today);

        return daysOverdue > 0 && daysOverdue % 7 == 0;
    }

    private void applyPenaltyAndMarkDefault(Loan loan, LocalDate today) {
        BigDecimal approvedAmount = loan.getApprovedAmount();
        BigDecimal rate = getTieredRate(approvedAmount);

        BigDecimal penaltyAmount = approvedAmount.multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);

        loan.setStatus(LoanStatus.DEFAULTED);
        BigDecimal currentTotal = loan.getTotalPayable() != null ? loan.getTotalPayable() : BigDecimal.ZERO;
        loan.setTotalPayable(currentTotal.add(penaltyAmount));

        loanRepository.save(loan);

        LoanPenalty lp = new LoanPenalty();
        lp.setLoan(loan);
        lp.setAmount(penaltyAmount);
        lp.setPenaltyDate(today);
        lp.setDaysLate(ChronoUnit.DAYS.between(loan.getDueDate(), today));

        loanPenaltyRepository.save(lp);

        String message = String.format(
            "Notice: A weekly penalty of %s has been applied to your loan. Your new total payable is %s.",
            penaltyAmount, currentTotal.add(penaltyAmount).toString()
        );

        emailService.sendMail(loan.getMember().getEmail(), "LOAN PENALTY", message);
    }

    private BigDecimal getTieredRate(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("5000")) <= 0) return new BigDecimal("0.03");
        if (amount.compareTo(new BigDecimal("10000")) <= 0) return new BigDecimal("0.04");
        return new BigDecimal("0.05");
    }

    private void sendReminder(Loan loan, String subject) {
        String content = "Dear " + loan.getMember().getFullName() + ",\n\n" +
                "This is a reminder regarding your loan of " + loan.getApprovedAmount() + ".\n" +
                "Due Date: " + loan.getDueDate() + ".\n" +
                "Total Payable: " + loan.getTotalPayable() + ".\n\n" +
                "Please ensure timely payment to avoid penalties.";

        emailService.sendMail(loan.getMember().getEmail(), subject, content);
    }

    @Transactional
    public void repayLoan(Long loanId, BigDecimal amount) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

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

    public List<LoanDto> getLoans(LoanStatus loanStatus, Pageable pageable) {
        try {
            List<LoanDto> dtoList = new ArrayList<>();
            Page<Tuple> loans = loanRepository.getActiveLoans(loanStatus, pageable);
            for (Tuple loan : loans) {
                LoanDto dto = LoanDto.builder()
                        .id(loan.get(0, Long.class))
                        .memberName(loan.get(1, String.class))
                        .requestedAmount(loan.get(2, BigDecimal.class))
                        .approvedAmount(loan.get(3, BigDecimal.class))
                        .totalPayableAmount(loan.get(4, BigDecimal.class))
                        .dueDate(loan.get(5, LocalDate.class))
                        .interestAmount(loan.get(6, BigDecimal.class))
                        .status(loan.get(7, LoanStatus.class))
                        .duration(loan.get(8, Integer.class))
                        .requestDate(loan.get(9, LocalDate.class))
                        .approvedDate(loan.get(10, LocalDate.class))
                        .memberNo(loan.get(11, String.class))
                        .memberId(loan.get(12, Long.class))
                        .eligibleAmount(loan.get(13, BigDecimal.class))
                        .balance(loan.get(14, BigDecimal.class))
                        .repaidDate(loan.get(15, LocalDate.class))
                        .build();
                dtoList.add(dto);
            }
            return dtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StkPushResponse initiateLoanPayment(Long loanId, BigDecimal amount) {
        try{
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Loan not found");
                throw new IllegalArgumentException("Amount must be greater than zero");
            }

            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

            if (loan.getStatus() != LoanStatus.DISBURSED
                    && loan.getStatus() != LoanStatus.DEFAULTED) {
                log.error("Loan is not active");
                throw new IllegalStateException("Loan is not active");
            }
            log.info("Loan found: {}", loan.getId());

            Member member = loan.getMember();

            log.info("====Attempting stk push====");
            StkPushResponse mpesaResponse = mpesaService.stkPush(
                    formatPhone.formatPhoneNumber(member.getPhone()),
                    amount.toString(),
                    "LOAN_" + member.getMemberNumber(),
                    "LOAN_" + loan.getId()
            );

            Transaction txn = new Transaction();
            txn.setCheckoutRequestID(mpesaResponse.getCheckoutRequestID());
            txn.setMember(member);
            txn.setAmount(amount);
            txn.setLoan(loan);
            txn.setType(TransactionType.LOAN);
            txn.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(txn);

            return mpesaResponse;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
