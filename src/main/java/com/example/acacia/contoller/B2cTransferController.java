package com.example.acacia.contoller;

import com.example.acacia.dto.B2cTransferDto;
import com.example.acacia.service.B2cTransferService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/b2c-transfers")
@RequiredArgsConstructor
public class B2cTransferController {
    private final B2cTransferService b2cTransferService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateTransfer(
            @RequestParam BigDecimal amount,
            @RequestParam String recipientPhone,
            @RequestParam String reason,
            @RequestParam String pin,
            @RequestParam Long memberId
    ) {
        try {
            B2cTransferDto dto = b2cTransferService.initiateTransfer(amount, recipientPhone, reason, pin, memberId);
            return ResponseHandler.responseBuilder("Transfer initiated successfully", HttpStatus.CREATED, dto);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @PostMapping("/{requestId}/authorize")
    public ResponseEntity<?> authorizeTransfer(
            @PathVariable Long requestId,
            @RequestParam Long memberId,
            @RequestParam boolean approve
    ) {
        try {
            B2cTransferDto dto = b2cTransferService.authorizeTransfer(requestId, memberId, approve);
            return ResponseHandler.responseBuilder("Transfer authorization processed", HttpStatus.OK, dto);
        } catch (Exception e) {
            return ResponseHandler.responseBuilder(e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @GetMapping
    public ResponseEntity<List<B2cTransferDto>> getAllTransfers() {
        return ResponseEntity.ok(b2cTransferService.getAllTransfers());
    }
}
