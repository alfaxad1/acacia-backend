package com.example.acacia.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class StkCallbackPayload {
    private Body body; // Use lowercase field names for standard JSON mapping

    @Getter
    @Setter
    public static class Body {
        private StkCallback stkCallback;
    }

    @Getter
    @Setter
    public static class StkCallback {
        private String merchantRequestID;
        private String checkoutRequestID;
        private Integer resultCode; // Changed to Integer to match your '== 0' check
        private String resultDesc;
        private CallbackMetadata callbackMetadata;
    }

    @Getter
    @Setter
    public static class CallbackMetadata {
        private List<Item> item;
    }

    @Getter
    @Setter
    public static class Item {
        private String name;
        private Object value; // Value can be String or Number
    }
}