package com.example.acacia.repository;

import com.example.acacia.enums.LoanStatus;
import com.example.acacia.model.Loan;
import com.example.acacia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatusAndDueDateBefore(LoanStatus loanStatus, LocalDate now);

    @Query("select count(l) from Loan l where l.member = :member and l.status =:status")
    int countDefaults(Member member, LoanStatus status);

    @Query("select sum(l.approvedAmount) from Loan l where l.status = :loanStatus")
    BigDecimal sumLoansIssued(LoanStatus loanStatus);

    @Query("select count(l) from Loan l where l.status = :loanStatus")
    long countActive(LoanStatus loanStatus);
}
