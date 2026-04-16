package com.example.acacia.service;

import com.example.acacia.config.MpesaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.acacia.dto.*;
import okhttp3.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MpesaService {

    private final MpesaConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private Instant tokenExpiry = Instant.now();

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

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String body = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException("STK Push failed: " + body);
            }
            return objectMapper.readValue(body, StkPushResponse.class);
        }
    }

}