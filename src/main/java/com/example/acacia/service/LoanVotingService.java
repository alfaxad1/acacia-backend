package com.example.acacia.service;

import com.example.acacia.Exception.ResourceNotFoundException;
import com.example.acacia.enums.LoanStatus;
import com.example.acacia.enums.MemberStatus;
import com.example.acacia.enums.VoteDecision;
import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanVote;
import com.example.acacia.model.Member;
import com.example.acacia.repository.LoanRepository;
import com.example.acacia.repository.LoanVoteRepository;
import com.example.acacia.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoanVotingService {

    private final LoanRepository loanRepository;
    private final LoanVoteRepository voteRepository;
    private final MemberRepository committeeRepository;
    private final LoanService loanService;
    private final EmailService emailService;

    @Transactional
    public void vote(
            Long loanId,
            Long committeeMemberId,
            VoteDecision decision
    ) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Voting closed");
        }

        Member member = committeeRepository.findById(committeeMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee member not found"));

        if (member.getStatus() ==  MemberStatus.EXITED || member.getStatus() ==  MemberStatus.SUSPENDED) {
            throw new IllegalStateException("Inactive committee member");
        }

        boolean voted = voteRepository
                .existsByLoanAndCommitteeMember(loan, member);

        if (voted) {
            throw new IllegalStateException("Already voted");
        }

        LoanVote vote = new LoanVote();
        vote.setLoan(loan);
        vote.setCommitteeMember(member);
        vote.setDecision(decision);
        vote.setVotedAt(LocalDateTime.now());

        voteRepository.save(vote);

        evaluateVotes(loan);
    }

    private void evaluateVotes(Loan loan) {

        long totalCommittee = committeeRepository.countActive(MemberStatus.ACTIVE);
        long majority = (totalCommittee / 2) + 1;

        long approvals = voteRepository.countByLoanAndDecision(
                loan, VoteDecision.APPROVE
        );

        long rejections = voteRepository.countByLoanAndDecision(
                loan, VoteDecision.REJECT
        );

        if (approvals >= majority) {
            loanService.approveLoan(loan.getId());
            emailService.sendMail(loan.getMember().getEmail(), "LOAN APPROVAL", "Your loan application has been approved. Please contact the treasurer for the disbursement");
        } else if (rejections >= majority) {
            loanService.rejectLoan(loan.getId(), "Committee rejected");
            emailService.sendMail(loan.getMember().getEmail(), "LOAN REJECTION", "Your loan application has been rejected by the members");
        }
    }
}

