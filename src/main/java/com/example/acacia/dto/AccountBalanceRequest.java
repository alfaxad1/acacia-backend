package com.example.acacia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountBalanceRequest {
    @JsonProperty("Initiator") private String initiator;
    @JsonProperty("SecurityCredential") private String securityCredential;
    @JsonProperty("CommandID") private String commandID;
    @JsonProperty("PartyA") private String partyA;
    @JsonProperty("IdentifierType") private String identifierType;
    @JsonProperty("Remarks") private String remarks;
    @JsonProperty("QueueTimeOutURL") private String queueTimeOutURL;
    @JsonProperty("ResultURL") private String resultURL;
}
