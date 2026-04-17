package com.example.acacia.contoller;

import com.example.acacia.dto.StkCallbackPayload;
import com.example.acacia.dto.StkPushResponse;
import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.enums.TransactionType;
import com.example.acacia.model.Member;
import com.example.acacia.model.Transaction;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.TransactionRepository;
import com.example.acacia.service.ContributionService;
import com.example.acacia.service.FineService;
import com.example.acacia.service.LoanService;
import com.example.acacia.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequiredArgsConstructor
public class MpesaCallbackController {
    private final MpesaService mpesaService;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final ContributionService contributionService;
    private final FineService fineService;
    private final LoanService loanService;

    @PostMapping("/stk/callback")
    public ResponseEntity<Map<String, Object>> stkCallback(@RequestBody StkCallbackPayload payload) {
        log.info("STK CALLBACK RECEIVED");
        var callbackData = payload.getBody().getStkCallback();
        String checkoutId = callbackData.getCheckoutRequestID();

        Transaction txn = transactionRepository.findByCheckoutRequestID(checkoutId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        log.info("Callback CheckoutID: {}", checkoutId);
        log.info("Saved CheckoutID: {}", txn.getCheckoutRequestID());
        log.info("ResultCode: {}", callbackData.getResultCode());
        if (callbackData.getResultCode() == 0) {
            if(txn.getType().equals(TransactionType.CONTRIBUTION)){
                txn.setStatus(TransactionStatus.COMPLETED);
                Member member = txn.getMember();

                log.info("Starting to record contribution for member...: {}", member.getId());
                contributionService.addContribution(
                        txn.getPeriod().getId(),
                        member.getId(),
                        LocalDateTime.now(),
                        txn.getAmount()
                );
                log.info("Contribution successful for member: {}", member.getId());
            } else if (txn.getType().equals(TransactionType.FINE)) {
                txn.setStatus(TransactionStatus.COMPLETED);

                log.info("Starting to record fine...");
                fineService.settleFine(txn.getFine().getId());
                log.info("Fine settled successfully");
            } else if (txn.getType().equals(TransactionType.LOAN)) {
                txn.setStatus(TransactionStatus.COMPLETED);

                log.info("Starting to save loan...");
                loanService.repayLoan(txn.getLoan().getId(), txn.getAmount());
                log.info("Loan saved...");
            }

        } else {
            txn.setStatus(TransactionStatus.FAILED);
            log.warn("Payment failed for request: {}", checkoutId);
        }

        transactionRepository.save(txn);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Success"));
    }

    @GetMapping("/transaction-status/{checkoutRequestId}")
    public ResponseEntity<TransactionStatus> checkStatus(@PathVariable String checkoutRequestId) {
        return transactionRepository.findByCheckoutRequestID(checkoutRequestId)
                .map(txn -> ResponseEntity.ok(txn.getStatus()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/c2b/confirmation")
    public ResponseEntity<Map<String, Object>> c2bConfirmation(@RequestBody Object payload) {
        log.info("C2B Confirmation: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    @PostMapping("/c2b/validation")
    public ResponseEntity<Map<String, Object>> c2bValidation(@RequestBody Object payload) {
        log.info("C2B Validation: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }
}
