package com.example.acacia.service;

import com.example.acacia.dto.ContributionArrearDto;
import com.example.acacia.dto.ContributionRequest;
import com.example.acacia.dto.ContributionResponseDTO;
import com.example.acacia.dto.Response;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContributionService {
    void addContribution(ContributionRequest request);
    List<ContributionResponseDTO> getAllContributions(Pageable pageable);
    ContributionResponseDTO getContributionById(Long id);

    Response<List<ContributionArrearDto>> getArrears(Pageable pageable);
}
