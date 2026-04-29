package com.example.acacia.service;

import com.example.acacia.config.MpesaConfig;
import com.example.acacia.enums.LoanStatus;
import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.model.B2cTransactions;
import com.example.acacia.model.Loan;
import com.example.acacia.model.SaccoWallet;
import com.example.acacia.repository.B2cTransactionsRepository;
import com.example.acacia.repository.LoanRepository;
import com.example.acacia.repository.SaccoWalletRepository;
import com.example.acacia.utility.FormatPhone;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.acacia.dto.*;
import okhttp3.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MpesaService {
    private final LoanRepository loanRepository;
    private final B2cTransactionsRepository b2cTransactionsRepository;
    private final SaccoWalletRepository walletRepository;

    private static final Logger logger = LoggerFactory.getLogger(MpesaService.class);

    private final MpesaConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private Instant tokenExpiry = Instant.now();

    private final OkHttpClient client = new OkHttpClient();

    private final FormatPhone formatPhone;

    private String getAccessToken() throws IOException, java.io.IOException {
        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken;
        }

        String auth = Base64.getEncoder().encodeToString(
                (config.getConsumerKey() + ":" + config.getConsumerSecret()).getBytes()
        );

        String url = config.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Basic " + auth)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get access token: " + body);
            }

            JsonNode json = objectMapper.readTree(body);
            accessToken = json.get("access_token").asText();
            tokenExpiry = Instant.now().plusSeconds(json.get("expires_in").asLong() - 60);

            logger.info("Access token refreshed, expires in {} seconds", json.get("expires_in").asLong());
            return accessToken;
        }
    }

    public StkPushResponse stkPush(String phoneNumber, String amount,
                                   String accountReference, String transactionDesc) throws IOException, java.io.IOException {

        logger.info("Formated phone {}", phoneNumber);

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String password = Base64.getEncoder().encodeToString(
                (config.getShortcode() + config.getPasskey() + timestamp).getBytes()
        );

        StkPushRequest requestBody = StkPushRequest.builder()
                .businessShortCode(config.getShortcode())
                .password(password)
                .timestamp(timestamp)
                .transactionType("CustomerPayBillOnline")
                .amount(amount)
                .partyA(phoneNumber)
                .partyB(config.getShortcode())
                .phoneNumber(phoneNumber)
                .callBackURL(config.getStkCallbackUrl())
                .accountReference(accountReference)
                .transactionDesc(transactionDesc)
                .build();

        String json = objectMapper.writeValueAsString(requestBody);

        String url = config.getBaseUrl() + "/mpesa/stkpush/v1/processrequest";
        logger.info("STK url: {}", url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String body = response.body().string();
            logger.info("STK body: {}", body);
            if (!response.isSuccessful()) {
                logger.error("STK response code: {}", response.body().string());
                throw new RuntimeException("STK Push failed: " + body);
            }
            return objectMapper.readValue(body, StkPushResponse.class);
        }
    }

    public void checkAccountBalance() throws Exception {
        AccountBalanceRequest requestBody = AccountBalanceRequest.builder()
                .initiator(config.getInitiatorName())
                .securityCredential(config.getB2cSecurityCredential())
                .commandID("AccountBalance")
                .partyA(config.getB2cShortcode())
                .identifierType("4")
                .remarks("Syncing balance")
                .queueTimeOutURL(config.getBalanceTimeoutUrl())
                .resultURL(config.getBalanceResultUrl())
                .build();

        sendMpesaRequest(config.getBaseUrl() + "/mpesa/accountbalance/v1/query", requestBody);
    }

    public void disburseFunds(String phoneNumber, BigDecimal amount, Loan loan) throws Exception {
        logger.info("Starting funds disbursement to phone: {} ...", formatPhone.formatPhoneNumber(phoneNumber));
        logger.info("Amount: {}", amount.toPlainString());
        B2CRequest requestBody = B2CRequest.builder()
                .initiatorName(config.getInitiatorName())
                .securityCredential(config.getB2cSecurityCredential())
                .commandID("BusinessPayment")
                .amount(amount.toPlainString())
                .partyA(config.getB2cShortcode())
                .partyB(formatPhone.formatPhoneNumber(phoneNumber))
                .remarks("Loan ID: " + loan.getId())
                .queueTimeOutURL(config.getB2cTimeoutUrl())
                .resultURL(config.getB2cResultUrl())
                .occasion("Loan Disbursement")
                .build();

        B2cTransactions txn = new B2cTransactions();
        txn.setAmount(amount);
        txn.setLoan(loan);
        txn.setRecipientPhone(phoneNumber);
        txn.setStatus(TransactionStatus.PENDING);
        B2cTransactions savedTxn = b2cTransactionsRepository.save(txn);

        try {
            logger.info("Sending request to mpesa...");
            JsonNode mpesaResponse = sendMpesaRequest(config.getBaseUrl() + "/mpesa/b2c/v1/paymentrequest", requestBody);
            if(mpesaResponse.has("ConversationID")){
                String conversationId = mpesaResponse.get("ConversationID").asText();
                String originConversationId = mpesaResponse.get("OriginatorConversationID").asText();

                savedTxn.setConversationId(conversationId);
                savedTxn.setOriginatorConversationId(originConversationId);
                b2cTransactionsRepository.save(savedTxn);

                logger.info("Transaction updated with ConversationID: {}", conversationId);
            }
        } catch (Exception e) {
            logger.error("Disbursement failed: {}", e.getMessage());
            savedTxn.setStatus(TransactionStatus.FAILED);
            b2cTransactionsRepository.save(savedTxn);
            throw new RuntimeException(e);
        }

    }

    private JsonNode sendMpesaRequest(String url, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBodyString = response.body() != null ? response.body().string() : "{}";
            //logger.info("Mpesa Response: {}", responseBodyString);

            if (!response.isSuccessful()) {
                logger.error("Mpesa API error: {} ", responseBodyString);
                throw new RuntimeException("Mpesa API error: " + responseBodyString);
            }

            return objectMapper.readTree(responseBodyString);
        }
    }

    public void processb2cCallback(String conversationId, String transactionId, MpesaCallbackResponse.ResultParameters resultParameters, Integer resultCode, String resultDesc) {
        try {
            B2cTransactions txn = b2cTransactionsRepository.findByConversationId(((conversationId)));
            if(txn != null){
                if(resultCode == 0){
                    BigDecimal afterBalance = resultParameters.getResultParameter().stream()
                            .filter(p -> "B2CWorkingAccountAvailableFunds".equals(p.getKey()))
                            .findFirst()
                            .map(p -> new BigDecimal(String.valueOf(p.getValue())))
                            .orElse(BigDecimal.ZERO);

                    SaccoWallet wallet = walletRepository.findById(1L)
                            .orElseThrow(() -> new IllegalStateException("Sacco wallet not configured"));

                    BigDecimal beforeBalance = wallet.getMpesaFloatBalance();

                    BigDecimal totalDeducted = beforeBalance.subtract(afterBalance);
                    BigDecimal fee = totalDeducted.subtract(txn.getAmount());

                    wallet.setMpesaFloatBalance(afterBalance);
                    walletRepository.save(wallet);

                    txn.setFee(fee);
                    txn.setStatus(TransactionStatus.COMPLETED);
                    txn.setTransactionId(transactionId);

                    Loan loan = loanRepository.findById(txn.getLoan().getId())
                            .orElseThrow(()-> new RuntimeException("Loan no loan found with that conversationID"));

                    BigDecimal interest = loan.getInterestAmount();
                    BigDecimal totalAMount = loan.getApprovedAmount().add(interest).add(fee);
                    BigDecimal c2bFee = totalAMount.multiply(BigDecimal.valueOf(0.0055));

                    loan.setC2bFee(c2bFee);
                    loan.setTotalPayable(totalAMount.add(c2bFee));
                    loan.setStatus(LoanStatus.DISBURSED);
                    loan.setTransactionCost(fee);
                    loanRepository.save(loan);
                }
                else {
                    logger.warn("B2C Disbursement failed for ConvID {}: {}", conversationId, resultDesc);
                    txn.setStatus(TransactionStatus.FAILED);
                    txn.setErrorReason(resultDesc);
                }
                b2cTransactionsRepository.save(txn);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}