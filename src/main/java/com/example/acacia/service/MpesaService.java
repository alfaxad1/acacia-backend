package com.example.acacia.service;

import com.example.acacia.config.MpesaConfig;
import com.example.acacia.utility.FormatPhone;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.stereotype.Service;

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
@Slf4j
public class MpesaService {

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

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get access token: " + response.body().string());
            }

            JsonNode json = objectMapper.readTree(response.body().string());
            accessToken = json.get("access_token").asText();
            tokenExpiry = Instant.now().plusSeconds(json.get("expires_in").asLong() - 60);
            return accessToken;
        }
    }

    public StkPushResponse stkPush(String phoneNumber, String amount,
                                   String accountReference, String transactionDesc) throws IOException, java.io.IOException {

        log.info("Formated phone {}", phoneNumber);

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
        log.info("STK url: {}", url);


        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String body = response.body().string();
            log.info("STK body: {}", body);
            if (!response.isSuccessful()) {
                log.error("STK response code: {}", response.body().string());
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

    public void disburseFunds(String phoneNumber, BigDecimal amount, String loanId) throws Exception {
        B2CRequest requestBody = B2CRequest.builder()
                .initiatorName(config.getInitiatorName())
                .securityCredential(config.getB2cSecurityCredential())
                .commandID("BusinessPayment")
                .amount(amount.toPlainString())
                .partyA(config.getB2cShortcode())
                .partyB(formatPhone.formatPhoneNumber(phoneNumber))
                .remarks("Loan ID: " + loanId)
                .queueTimeOutURL(config.getB2cTimeoutUrl())
                .resultURL(config.getB2cResultUrl())
                .occasion("Loan Disbursement")
                .build();

        sendMpesaRequest(config.getBaseUrl() + "/mpesa/b2c/v1/paymentrequest", requestBody);
    }

    private void sendMpesaRequest(String url, Object body) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new RuntimeException(response.body().string());
        }
    }

    /**
     * Disburse funds via B2C
     */
    public void disburseFunds(String phoneNumber, String amount, String remarks) throws Exception {
        String securityCredential = config.getB2cSecurityCredential();

        B2CRequest requestBody = B2CRequest.builder()
                .initiatorName(config.getInitiatorName())
                .securityCredential(securityCredential)
                .commandID("BusinessPayment")
                .amount(amount)
                .partyA(config.getB2cShortcode())
                .partyB(phoneNumber)
                .remarks(remarks)
                .queueTimeOutURL(config.getB2cTimeoutUrl())
                .resultURL(config.getB2cResultUrl())
                .occasion("SACCO Payout")
                .build();

        sendMpesaRequest(config.getBaseUrl() + "/mpesa/b2c/v1/paymentrequest", requestBody);
    }

}