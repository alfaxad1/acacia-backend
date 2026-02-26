package com.example.acacia.contoller;

import com.example.acacia.dto.LoanDto;
import com.example.acacia.enums.LoanStatus;
import com.example.acacia.enums.VoteDecision;
import com.example.acacia.model.Loan;
import com.example.acacia.service.LoanService;
import com.example.acacia.service.LoanVotingService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan")
public class LoanController {
    private final LoanService loanService;
    private final LoanVotingService loanVotingService;

    @GetMapping()
    public ResponseEntity<List<LoanDto>> getLoans(@RequestParam LoanStatus loanStatus, @RequestParam(defaultValue = "10") Integer size, @RequestParam(defaultValue = "0") Integer page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(loanService.getLoans(loanStatus, pageable));
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestLoan(
            @RequestParam Long memberId,
            @RequestParam BigDecimal amount
    ) {
        loanService.requestLoan(memberId, amount);
        return ResponseHandler.responseBuilder("loan applied successfully", HttpStatus.OK, null);
    }

    @PostMapping("/{loanId}/vote")
    public ResponseEntity<?> vote(
            @PathVariable Long loanId,
            @RequestParam Long memberId,
            @RequestParam VoteDecision decision
    ) {
        loanVotingService.vote(loanId, memberId, decision);
        return ResponseEntity.ok("Vote recorded");
    }

    @PostMapping("/disburse")
    public ResponseEntity<?> disburseLoan(
            @RequestParam Long loanId
    ) {
        loanService.disburseLoan(loanId);
        return ResponseHandler.responseBuilder("loan disbursed successfully", HttpStatus.OK, null);
    }

    @PostMapping("/repay")
    public ResponseEntity<?> repayLoan(
            @RequestParam Long loanId,
            @RequestParam BigDecimal amount
    ) {
        loanService.repayLoan(loanId, amount);
        return ResponseHandler.responseBuilder("loan repaid successfully", HttpStatus.OK, null);
    }

    @PostMapping("/appy-penalty")
    public ResponseEntity<?> applyPenalty() {
        loanService.applyLoanPenalties();
        return ResponseEntity.ok().build();
    }


}
