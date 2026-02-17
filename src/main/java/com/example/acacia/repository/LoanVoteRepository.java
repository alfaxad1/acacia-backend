package com.example.acacia.repository;

import com.example.acacia.enums.VoteDecision;
import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanVote;
import com.example.acacia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanVoteRepository extends JpaRepository<LoanVote,Long> {
    boolean existsByLoanAndCommitteeMember(Loan loan, Member committeeMember);

    long countByLoanAndDecision(Loan loan, VoteDecision voteDecision);
}
