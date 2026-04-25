package com.example.acacia.contoller;

import com.example.acacia.dto.MpesaCallbackResponse;
import com.example.acacia.dto.StkCallbackPayload;
import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.enums.TransactionType;
import com.example.acacia.model.B2cTransactions;
import com.example.acacia.model.Member;
import com.example.acacia.model.SaccoWallet;
import com.example.acacia.model.Transaction;
import com.example.acacia.repository.B2cTransactionsRepository;
import com.example.acacia.repository.SaccoWalletRepository;
import com.example.acacia.repository.TransactionRepository;
import com.example.acacia.service.ContributionService;
import com.example.acacia.service.FineService;
import com.example.acacia.service.LoanService;
import com.example.acacia.service.MpesaService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MpesaCallbackController {
    private final MpesaService mpesaService;
    private final TransactionRepository transactionRepository;
    private final SaccoWalletRepository walletRepository;
    private final ContributionService contributionService;
    private final FineService fineService;
    private final LoanService loanService;
    private final B2cTransactionsRepository b2cTransactionsRepository;

    private static final Logger logger = LoggerFactory.getLogger(MpesaCallbackController.class);

    @PostMapping("/stk/callback")
    public ResponseEntity<Map<String, Object>> stkCallback(@RequestBody StkCallbackPayload payload) {
        logger.info("STK CALLBACK RECEIVED");
        var callbackData = payload.getBody().getStkCallback();
        String checkoutId = callbackData.getCheckoutRequestID();

        Transaction txn = transactionRepository.findByCheckoutRequestID(checkoutId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        logger.info("Callback CheckoutID: {}", checkoutId);
        if (callbackData.getResultCode() == 0) {
            if(txn.getType().equals(TransactionType.CONTRIBUTION)){
                txn.setStatus(TransactionStatus.COMPLETED);
                Member member = txn.getMember();

                logger.info("Starting to record contribution for member...: {}", member.getId());
                contributionService.addContribution(
                        txn.getPeriod().getId(),
                        member.getId(),
                        LocalDateTime.now(),
                        txn.getAmount()
                );
                logger.info("Contribution successful for member: {}", member.getId());
            } else if (txn.getType().equals(TransactionType.FINE)) {
                txn.setStatus(TransactionStatus.COMPLETED);

                fineService.settleFine(txn.getFine().getId());
                logger.info("Fine settled successfully");
            } else if (txn.getType().equals(TransactionType.LOAN)) {
                txn.setStatus(TransactionStatus.COMPLETED);

                loanService.repayLoan(txn.getLoan().getId(), txn.getAmount());
                logger.info("Loan saved...");
            }

        } else {
            txn.setStatus(TransactionStatus.FAILED);
            logger.warn("Payment failed for request: {}", checkoutId);
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
        logger.info("C2B Confirmation: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    @PostMapping("/c2b/validation")
    public ResponseEntity<Map<String, Object>> c2bValidation(@RequestBody Object payload) {
        logger.info("C2B Validation: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    @PostMapping("/mpesa-callbacks/balance/result")
    public void handleBalanceResult(@RequestBody MpesaCallbackResponse response) {
        if (response.getResult().getResultCode() == 0) {
            String balanceData = (String) response.getResult().getResultParameters().getResultParameter()
                    .stream().filter(p -> p.getKey().equals("AccountBalance")).findFirst().get().getValue();

            // Split: Name|Currency|Available|Reserved|Unused|Unused
            String availableBalance = balanceData.split("\\|")[2];

            SaccoWallet wallet = walletRepository.findById(1L).orElse(new SaccoWallet());
            wallet.setMpesaFloatBalance(new BigDecimal(availableBalance));
            wallet.setLastUpdated(LocalDateTime.now());
            walletRepository.save(wallet);
        }
    }
    @PostMapping("/mpesa-callbacks/balance/timeout")
    public ResponseEntity<?> handleBalanceTimeout(@RequestBody JsonNode timeoutResponse) {
        logger.warn("M-PESA Balance Query Timed Out: {}", timeoutResponse.toString());
        return ResponseEntity.ok("Timeout Received");
    }

    @PostMapping("/mpesa-callbacks/b2c/result")
    public ResponseEntity<?> handleB2cResult(@RequestBody MpesaCallbackResponse response) {
       try{
           logger.info("Mpesa callback received. Response: {}", response.toString());
           mpesaService.processb2cCallback(
                   response.getResult().getConversationID(),
                   response.getResult().getTransactionID(),
                   response.getResult().getResultParameters(),
                   response.getResult().getResultCode(),
                   response.getResult().getResultDesc()
           );
           return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
       } catch (Exception e) {
           logger.error("B2C callback processing failed: {}", e.getMessage(), e);
           throw new RuntimeException(e);
       }
    }

    @PostMapping("/mpesa-callbacks/b2c/timeout")
    public ResponseEntity<?> handleB2cTimeout(@RequestBody JsonNode timeoutResponse) {
        logger.warn("M-PESA B2C TIMEOUT RECEIVED: {}", timeoutResponse.toString());

        String conversationId = timeoutResponse.at("/Result/ConversationID").asText();

        if (conversationId.isEmpty()) {
            conversationId = timeoutResponse.at("/ConversationID").asText();
        }

        B2cTransactions txn = b2cTransactionsRepository.findByConversationId(conversationId);
        txn.setStatus(TransactionStatus.IN_DOUBT);
            txn.setErrorReason("Safaricom Timeout - Manual Verification Required");
            b2cTransactionsRepository.save(txn);

            logger.error("Transaction for Loan {} is in-doubt. DO NOT RE-INITIATE without checking M-Pesa Portal.",
                    txn.getLoan().getId());

        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Timeout Recorded"));
    }
}
