package com.example.acacia.contoller;

import com.example.acacia.dto.ContributionPeriodDto;
import com.example.acacia.dto.ContributionPeriodRequest;
import com.example.acacia.dto.Response;
import com.example.acacia.enums.ResponseStatusEnum;
import com.example.acacia.service.ContributionPeriodService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Response<List<ContributionPeriodDto>>> getAllContributionPeriods(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Response<List<ContributionPeriodDto>> response = contributionPeriodService.getContributionPeriods(pageable);
        if (response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)) {
            return ResponseEntity.ok().body(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping()
    public ResponseEntity<?> save(@RequestBody ContributionPeriodRequest request) {
        contributionPeriodService.addPeriod(request.getDate());
        return ResponseHandler.responseBuilder("period added successfully", HttpStatus.CREATED, null);
    }

}
