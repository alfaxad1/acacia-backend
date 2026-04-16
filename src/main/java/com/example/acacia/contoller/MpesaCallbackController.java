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

    @PostMapping("/test-stk")
    public ResponseEntity<?> testStk(@RequestParam String phone, @RequestParam String amount) {
        try {
            System.out.println("=== Starting STK Push Test ===");
            System.out.println("Phone: " + phone + " | Amount: " + amount);

            StkPushResponse response = mpesaService.stkPush(
                    phone,
                    amount,
                    "TEST-" + System.currentTimeMillis(),
                    "Sandbox Test Payment"
            );

            System.out.println("STK Push Successful!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @PostMapping("/stk/callback")
    public ResponseEntity<Map<String, Object>> stkCallback(@RequestBody StkCallbackPayload payload) {
        var callbackData = payload.getBody().getStkCallback();
        String checkoutId = callbackData.getCheckoutRequestID();

        Transaction txn = transactionRepository.findByCheckoutRequestID(checkoutId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (callbackData.getResultCode() == 0 && txn.getType().equals(TransactionType.CONTRIBUTION)) {
            txn.setStatus(TransactionStatus.COMPLETED);
            Member member = txn.getMember();

            contributionService.addContribution(
                    txn.getPeriod().getId(),
                    member.getId(),
                    LocalDateTime.now(),
                    txn.getAmount()
            );
            log.info("Contribution successful for member: {}", member.getId());
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            log.warn("Payment failed for request: {}", checkoutId);
        }

        transactionRepository.save(txn);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Success"));
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
