package com.example.acacia.contoller;

import com.example.acacia.dto.FineDto;
import com.example.acacia.dto.FineRequest;
import com.example.acacia.enums.FineStatus;
import com.example.acacia.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fine")
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
    public ResponseEntity<?> settleFine(@RequestParam Long fineId) {
        fineService.settleFine(fineId);
        return ResponseEntity.ok("Fine settled");
    }

}
