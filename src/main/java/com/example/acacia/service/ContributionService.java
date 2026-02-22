package com.example.acacia.service;

import com.example.acacia.dto.ContributionRequest;
import com.example.acacia.dto.ContributionResponseDTO;
import com.example.acacia.model.Contribution;

import java.util.List;

public interface ContributionService {
    void addContribution(ContributionRequest request);
    List<ContributionResponseDTO> getAllContributions();
    ContributionResponseDTO getContributionById(Long id);
}
