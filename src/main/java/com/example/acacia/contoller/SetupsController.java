package com.example.acacia.contoller;

import com.example.acacia.model.SaccoSetups;
import com.example.acacia.service.SetupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/setup")
public class SetupsController {
    private final SetupsService setupsService;
    @GetMapping
    ResponseEntity<SaccoSetups> getSetups() {
        return ResponseEntity.ok().body(setupsService.getSetups());
    }

    @PutMapping
    ResponseEntity<?> editSetups(@RequestBody SaccoSetups saccoSetups) {
        setupsService.editSetups(saccoSetups);
        return ResponseEntity.ok().body("success");
    }
}
