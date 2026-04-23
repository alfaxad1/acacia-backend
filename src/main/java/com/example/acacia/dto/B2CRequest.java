package com.example.acacia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class B2CRequest {
    @JsonProperty("InitiatorName") private String initiatorName;
    @JsonProperty("SecurityCredential") private String securityCredential;
    @JsonProperty("CommandID") private String commandID;
    @JsonProperty("Amount") private String amount;
    @JsonProperty("PartyA") private String partyA;
    @JsonProperty("PartyB") private String partyB;
    @JsonProperty("Remarks") private String remarks;
    @JsonProperty("QueueTimeOutURL") private String queueTimeOutURL;
    @JsonProperty("ResultURL") private String resultURL;
    @JsonProperty("Occasion") private String occasion;
}

