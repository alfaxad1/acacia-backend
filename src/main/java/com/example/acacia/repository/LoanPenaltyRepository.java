package com.example.acacia.repository;

import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanPenalty;
import com.example.acacia.model.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPenaltyRepository extends JpaRepository<LoanPenalty,Long> {
    List<LoanRepayment> findByLoan(Loan loan);
}
