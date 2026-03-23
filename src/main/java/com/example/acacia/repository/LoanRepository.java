package com.example.acacia.repository;

import com.example.acacia.enums.LoanStatus;
import com.example.acacia.model.Loan;
import com.example.acacia.model.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatusAndDueDateBefore(LoanStatus loanStatus, LocalDate now);

    @Query("select count(l) from Loan l where l.member = :member and l.status =:status")
    int countDefaults(Member member, LoanStatus status);

    @Query("select coalesce(sum(l.approvedAmount), 0) from Loan l where l.status in :loanStatus")
    BigDecimal sumLoans(List<LoanStatus> loanStatus);

    @Query("select coalesce(sum(l.totalPayable), 0) from Loan l where l.status in :loanStatus")
    BigDecimal sumPaidLoans(List<LoanStatus> loanStatus);

    @Query("select count(l) from Loan l where l.status = :loanStatus")
    long countActive(LoanStatus loanStatus);

    @Query("select l.id, l.member.fullName, l.requestedAmount, " +
            "l.approvedAmount, l.totalPayable, l.dueDate, l.interestAmount, l.status, " +
            "l.durationDays, l.requestDate, l.approvalDate, l.member.memberNumber, l.member.id, l.eligibleAmount, " +
            "(l.totalPayable - COALESCE((select sum(p.amount) from LoanRepayment p where p.loan.id = l.id), 0)), l.repaidDate " +
            "from Loan l where l.status = :loanStatus")
    Page<Tuple> getActiveLoans(LoanStatus loanStatus, Pageable pageable);

    @Query("select count(l), coalesce(sum(l.totalPayable), 0) from Loan l join l.member m where m.id =:userId and l.status in :statuses")
    List<Object[]> getMemberLoansInfo(Long userId, List<LoanStatus> statuses);

    List<Loan> findByStatusAndDueDate(LoanStatus loanStatus, LocalDate sevenDaysFromNow);
}
