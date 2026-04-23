package com.example.acacia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MpesaCallbackResponse {
    @JsonProperty("Result")
    private Result result;

    @Data
    public static class Result {
        @JsonProperty("ResultCode") private Integer resultCode;
        @JsonProperty("ResultDesc") private String resultDesc;
        @JsonProperty("OriginatorConversationID") private String originatorConversationID;
        @JsonProperty("ConversationID") private String conversationID;
        @JsonProperty("TransactionID") private String transactionID;
        @JsonProperty("ResultParameters") private ResultParameters resultParameters;
    }

    @Data
    public static class ResultParameters {
        @JsonProperty("ResultParameter") private List<MpesaParameter> resultParameter;
    }

    @Data
    public static class MpesaParameter {
        @JsonProperty("Key") private String key;
        @JsonProperty("Value") private Object value;
    }
}
