package com.example.acacia.service;

import com.example.acacia.model.ContributionPeriod;

import java.time.LocalDate;
import java.util.List;

public interface ContributionPeriodService {
    List<ContributionPeriod> getContributionPeriods();

    void addPeriod(LocalDate day);
}
