package com.example.acacia.contoller;

import com.example.acacia.dto.ContributionRequest;
import com.example.acacia.service.ContributionPenaltyJobService;
import com.example.acacia.service.ContributionService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/contribution")
@RequiredArgsConstructor
public class ContributionController {
    private final ContributionService contributionService;
    private final ContributionPenaltyJobService penaltyJobService;

    @PostMapping
    ResponseEntity<?> recordContribution(@RequestBody ContributionRequest request) {
        contributionService.addContribution(request);
        return ResponseHandler.responseBuilder("contribution recorded successfully", HttpStatus.CREATED, null);
    }

//    @GetMapping
//    ResponseEntity<?> lateFine() {
//        penaltyJobService.applyLateContributionFines();
//        return ResponseEntity.ok().build();
//    }

}
