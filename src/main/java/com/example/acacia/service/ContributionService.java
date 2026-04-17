package com.example.acacia.service;

import com.example.acacia.dto.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ContributionService {
    void addContribution(Long periodId, Long memberId, LocalDateTime paymentDate, BigDecimal amount);
    List<ContributionResponseDTO> getAllContributions(Pageable pageable);
    ContributionResponseDTO getContributionById(Long id);

    StkPushResponse initiateContribution(Long memberId, Long periodId) throws IOException;

    Response<List<ContributionArrearDto>> getArrears(Pageable pageable);
}
