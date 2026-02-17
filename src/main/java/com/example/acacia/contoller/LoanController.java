package com.example.acacia.contoller;

import com.example.acacia.enums.VoteDecision;
import com.example.acacia.model.Loan;
import com.example.acacia.service.LoanService;
import com.example.acacia.service.LoanVotingService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/loan")
public class LoanController {
    private final LoanService loanService;
    private final LoanVotingService loanVotingService;

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
            @RequestParam Long committeeMemberId,
            @RequestParam VoteDecision decision
    ) {
        loanVotingService.vote(loanId, committeeMemberId, decision);
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
