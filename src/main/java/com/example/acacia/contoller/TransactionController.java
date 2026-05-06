package com.example.acacia.contoller;

import com.example.acacia.model.TransactionSummary;
import com.example.acacia.repository.TransactionSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionSummaryRepository repository;

    @GetMapping
    public ResponseEntity<Page<TransactionSummary>> getTransactions(
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) String search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC, size = 15) Pageable pageable) {

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(repository.findByMemberNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(search, search, pageable));
        }

        if (loanId != null) {
            return ResponseEntity.ok(repository.findByLoanId(loanId, pageable));
        }

        return ResponseEntity.ok(repository.findAll(pageable));
    }
}
