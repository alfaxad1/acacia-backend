package com.example.acacia.repository;

import com.example.acacia.model.Loan;
import com.example.acacia.model.LoanRepayment;
import com.example.acacia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment,Long> {
    @Query("""
    SELECT COALESCE(SUM(lr.amount), 0)
    FROM LoanRepayment lr
    WHERE lr.loan.member = :member
""")
    BigDecimal sumLoanRepayments(@Param("member") Member member);

    List<LoanRepayment> findByLoan(Loan loan);
}
