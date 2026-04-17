package com.example.acacia.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormatPhone {
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        String cleanNumber = phoneNumber.replaceAll("\\D", "");

        if (cleanNumber.startsWith("0")) {
            return "254" + cleanNumber.substring(1);
        } else if (cleanNumber.startsWith("7") || cleanNumber.startsWith("1")) {
            return "254" + cleanNumber;
        } else if (cleanNumber.startsWith("254")) {
            return cleanNumber;
        }

        return cleanNumber;
    }
}
