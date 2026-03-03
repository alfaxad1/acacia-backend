package com.example.acacia.repository;

import com.example.acacia.enums.AdjustmentType;
import com.example.acacia.model.AccountAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountAdjustmentRepository extends JpaRepository<AccountAdjustment,Long> {
    List<AccountAdjustment> findAllByType(AdjustmentType type);
}
