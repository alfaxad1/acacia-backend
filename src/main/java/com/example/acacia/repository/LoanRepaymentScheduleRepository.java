package com.example.acacia.repository;

import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanRepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentScheduleRepository extends JpaRepository<LoanRepaymentSchedule, Long> {
    List<LoanRepaymentSchedule> findUnpaidByLoan(Loan loan);

    int countUnpaidByLoan(Loan loan);

    List<LoanRepaymentSchedule> findByLoan(Loan loan);
}
