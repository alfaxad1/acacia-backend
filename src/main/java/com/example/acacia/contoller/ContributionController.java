package com.example.acacia.contoller;

import com.example.acacia.dto.ContributionArrearDto;
import com.example.acacia.dto.ContributionResponseDTO;
import com.example.acacia.dto.Response;
import com.example.acacia.dto.StkPushResponse;
import com.example.acacia.enums.ResponseStatusEnum;
import com.example.acacia.enums.TransactionStatus;
import com.example.acacia.repository.TransactionRepository;
import com.example.acacia.service.ContributionPenaltyJobService;
import com.example.acacia.service.ContributionService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/contribution")
@RequiredArgsConstructor
@Slf4j
public class ContributionController {
    private final ContributionService contributionService;
    private final TransactionRepository transactionRepository;
    private final ContributionPenaltyJobService penaltyJobService;

    @PostMapping
    ResponseEntity<?> recordContribution(
            @RequestParam Long periodId,
            @RequestParam Long memberId
    ) throws IOException {
        try{
            StkPushResponse mpesaResponse = contributionService.initiateContribution(memberId, periodId);

            Map<String, Object> data = new HashMap<>();
            data.put("checkoutRequestId", mpesaResponse.getCheckoutRequestID());

            return ResponseHandler.responseBuilder(
                    "STK push sent. Awaiting user payment confirmation",
                    HttpStatus.CREATED,
                    data
            );
        }catch (Exception e){
            log.error("Error saving contribution", e);
            throw new RuntimeException("Error saving contribution", e);
        }
    }

    @GetMapping
    public ResponseEntity<List<ContributionResponseDTO>> getAll(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(contributionService.getAllContributions(pageable));
    }

    @GetMapping("/arrears")
    public ResponseEntity<Response<List<ContributionArrearDto>>> getContributionArrears(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Response<List<ContributionArrearDto>> response = contributionService.getArrears(pageable);
        if (response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)) {
            return ResponseEntity.ok().body(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }


//    @GetMapping
//    ResponseEntity<?> lateFine() {
//        penaltyJobService.applyLateContributionFines();
//        return ResponseEntity.ok().build();
//    }

}
