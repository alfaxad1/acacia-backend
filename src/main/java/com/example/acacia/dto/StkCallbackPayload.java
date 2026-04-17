package com.example.acacia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StkCallbackPayload {

    @JsonProperty("Body")
    private Body body;

    @Getter
    @Setter
    public static class Body {

        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @Getter
    @Setter
    public static class StkCallback {

        @JsonProperty("MerchantRequestID")
        private String merchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestID;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("CallbackMetadata")
        private CallbackMetadata callbackMetadata;
    }

    @Getter
    @Setter
    public static class CallbackMetadata {

        @JsonProperty("Item")
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {

        @JsonProperty("Name")
        private String name;

        @JsonProperty("Value")
        private Object value;
    }
}