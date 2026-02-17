package com.example.acacia.utility;

import com.example.acacia.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberNumberGenerator {

    private final MemberRepository memberRepository;

    private static final String PREFIX = "ACA";
    private static final char START_LETTER = 'A';

    @Transactional
    public String generateMemberNumber() {

        String lastNumber = memberRepository.findLastMemberNumber();

        if (lastNumber == null) {
            return "ACA0A00";
        }

        // Remove prefix: ACA
        String body = lastNumber.substring(3);

        int firstDigit = Character.getNumericValue(body.charAt(0));
        char letter = body.charAt(1);
        int lastTwoDigits = Integer.parseInt(body.substring(2, 4));

        // Increment last two digits
        lastTwoDigits++;

        if (lastTwoDigits > 99) {
            lastTwoDigits = 0;
            letter++;
        }

        if (letter > 'Z') {
            letter = START_LETTER;
            firstDigit++;
        }

        if (firstDigit > 9) {
            throw new IllegalStateException("Member number limit reached");
        }

        return String.format(
                "%s%d%c%02d",
                PREFIX,
                firstDigit,
                letter,
                lastTwoDigits
        );
    }
}

