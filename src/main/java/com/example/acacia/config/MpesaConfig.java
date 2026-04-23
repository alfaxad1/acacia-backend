package com.example.acacia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Getter
@Setter
public class MpesaConfig {
    private boolean sandbox;
    private String consumerKey;
    private String consumerSecret;
    private String shortcode;
    private String passkey;
    private String paybill;
    private String baseUrl;
    private String stkCallbackUrl;
    private String c2bConfirmationUrl;
    private String c2bValidationUrl;
    private String initiatorName;
    private String b2cSecurityCredential;
    private String b2cShortcode;
    private String b2cResultUrl;
    private String b2cTimeoutUrl;
    private String balanceResultUrl;
    private String balanceTimeoutUrl;
}
