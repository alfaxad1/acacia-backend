package com.example.acacia.contoller;

import com.example.acacia.enums.AdjustmentType;
import com.example.acacia.model.AccountAdjustment;
import com.example.acacia.service.AccountAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/account-adjustment")
@RequiredArgsConstructor
public class AccountAdjustmentController {
    private final AccountAdjustmentService accountAdjustmentService;

    @PostMapping
    public ResponseEntity<String> addAdjustment(@RequestBody AccountAdjustment accountAdjustment){
        accountAdjustmentService.addAdjustment(accountAdjustment);
        return ResponseEntity.ok().body("recorded successfully");
    }

    @GetMapping
    public ResponseEntity<List<AccountAdjustment>> getAllAdjustment(@RequestParam AdjustmentType type) {
        return ResponseEntity.ok(accountAdjustmentService.getAdjustments(type));
    }
}
