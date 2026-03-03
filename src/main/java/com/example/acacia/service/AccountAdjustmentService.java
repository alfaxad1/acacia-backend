package com.example.acacia.service;

import com.example.acacia.enums.AdjustmentType;
import com.example.acacia.model.AccountAdjustment;
import com.example.acacia.repository.AccountAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountAdjustmentService {
    private final AccountAdjustmentRepository accountAdjustmentRepository;
    public void addAdjustment(AccountAdjustment accountAdjustment) {
        try{
            accountAdjustmentRepository.save(accountAdjustment);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<AccountAdjustment> getAdjustments(AdjustmentType type) {
        try {
            return accountAdjustmentRepository.findAllByType(type);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching adjustments", e);
        }
    }
}
