package com.example.acacia.contoller;

import com.example.acacia.dto.ExtraDto;
import com.example.acacia.dto.Response;
import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.enums.ResponseStatusEnum;
import com.example.acacia.service.ExtraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/extra")
public class ExtraController {
    private final ExtraService extraService;
    @GetMapping()
    public ResponseEntity<Response<List<ExtraDto>>> getExtras(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, ExtraType extraType){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Response<List<ExtraDto>> response = extraService.getExtras(pageable, extraType);
        if (response != null && response.getStatus().equals(ResponseStatusEnum.SUCCESS)) {
            return ResponseEntity.ok().body(response);
        }else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
