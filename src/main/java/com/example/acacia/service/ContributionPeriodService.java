package com.example.acacia.service;

import com.example.acacia.dto.ContributionPeriodDto;
import com.example.acacia.dto.Response;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ContributionPeriodService {
    Response<List<ContributionPeriodDto>> getContributionPeriods(Pageable pageable);

    void addPeriod(LocalDate day);
}
