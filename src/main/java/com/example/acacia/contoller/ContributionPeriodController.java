package com.example.acacia.contoller;

import com.example.acacia.dto.ContributionPeriodRequest;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.service.ContributionPeriodService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/contribution-period")
@RequiredArgsConstructor
public class ContributionPeriodController {
    private final ContributionPeriodService contributionPeriodService;

    @GetMapping
    public ResponseEntity<List<ContributionPeriod>> findAll() {
        return ResponseEntity.ok().body(contributionPeriodService.getContributionPeriods());
    }

    @PostMapping()
    public ResponseEntity<?> save(@RequestBody ContributionPeriodRequest request) {
        contributionPeriodService.addPeriod(request.getDay());
        return ResponseHandler.responseBuilder("period added successfully", HttpStatus.CREATED, null);
    }


}
