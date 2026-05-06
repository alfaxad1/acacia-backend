package com.example.acacia.repository;

import com.example.acacia.model.TransactionSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionSummaryRepository extends JpaRepository<TransactionSummary, String> {

    Page<TransactionSummary> findByMemberNameContainingIgnoreCaseOrReferenceContainingIgnoreCase(
            String name, String ref, Pageable pageable);

    Page<TransactionSummary> findByLoanId(Long loanId, Pageable pageable);
}
