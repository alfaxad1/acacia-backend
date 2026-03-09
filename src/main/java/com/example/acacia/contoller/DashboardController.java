package com.example.acacia.contoller;

import com.example.acacia.dto.DashboardSummary;
import com.example.acacia.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getSummary(@RequestParam Long memberId) {
        return ResponseEntity.ok().body(dashboardService.getDashboardSummary(memberId));
    }
}
