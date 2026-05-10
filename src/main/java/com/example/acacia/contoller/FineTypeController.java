package com.example.acacia.contoller;

import com.example.acacia.dto.FineTypeDto;
import com.example.acacia.dto.FineTypeRequest;
import com.example.acacia.service.FineTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finetypes")
@RequiredArgsConstructor
public class FineTypeController {

    private final FineTypeService fineTypeService;

    @PostMapping
    public ResponseEntity<FineTypeDto> createFineType(@RequestBody FineTypeRequest request) {
        return new ResponseEntity<>(fineTypeService.createFineType(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FineTypeDto>> getAllFineTypes() {
        return ResponseEntity.ok(fineTypeService.getAllFineTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FineTypeDto> getFineTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(fineTypeService.getFineTypeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FineTypeDto> updateFineType(@PathVariable Long id, @RequestBody FineTypeRequest request) {
        return ResponseEntity.ok(fineTypeService.updateFineType(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFineType(@PathVariable Long id) {
        fineTypeService.deleteFineType(id);
        return ResponseEntity.noContent().build();
    }
}
