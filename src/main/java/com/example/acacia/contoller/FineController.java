package com.example.acacia.contoller;

import com.example.acacia.dto.FineDto;
import com.example.acacia.dto.FineRequest;
import com.example.acacia.dto.StkPushResponse;
import com.example.acacia.enums.FineStatus;
import com.example.acacia.service.FineService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fine")
@Slf4j
public class FineController {
    private final FineService fineService;

    @PostMapping()
    public ResponseEntity<?> recordFine(@RequestBody FineRequest fineRequest) {
        fineService.recordFine(fineRequest);
        return ResponseEntity.ok("Fine recorded");
    }
    @GetMapping()
    public ResponseEntity<List<FineDto>> getAllFines(@RequestParam FineStatus status) {
        return ResponseEntity.ok(fineService.getFines(status));
    }

    @PostMapping("/settle")
    public ResponseEntity<?> settleFine(@RequestParam Long fineId) throws IOException {
        try{
            StkPushResponse mpesaResponse =  fineService.initiateFinePayment(fineId);

            Map<String, Object> data = new HashMap<>();
            data.put("checkoutRequestId", mpesaResponse.getCheckoutRequestID());

            return ResponseHandler.responseBuilder("STK push sent. Awaiting user payment confirmation", HttpStatus.CREATED, data);
        }catch (Exception e){
            log.error("Error saving fine", e);
            throw new RuntimeException("Error saving fine", e);
        }

    }

}
