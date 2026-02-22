package com.example.acacia.service;

import com.example.acacia.dto.ContributionPeriodDto;
import com.example.acacia.model.ContributionPeriod;

import java.time.LocalDate;
import java.util.List;

public interface ContributionPeriodService {
    List<ContributionPeriodDto> getContributionPeriods();

    void addPeriod(LocalDate day);
}
