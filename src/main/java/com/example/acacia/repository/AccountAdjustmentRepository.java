package com.example.acacia.repository;

import com.example.acacia.enums.AdjustmentType;
import com.example.acacia.model.AccountAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AccountAdjustmentRepository extends JpaRepository<AccountAdjustment,Long> {
    List<AccountAdjustment> findAllByType(AdjustmentType type);

    @Query("select coalesce(sum(adj.totalCost), 0) from AccountAdjustment adj where adj.type = :adjustmentType")
    BigDecimal sumAdjustments(AdjustmentType adjustmentType);
}
