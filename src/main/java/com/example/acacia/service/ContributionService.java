package com.example.acacia.service;

import com.example.acacia.dto.ContributionRequest;
import com.example.acacia.model.Contribution;

public interface ContributionService {
    void addContribution(ContributionRequest request);
}
