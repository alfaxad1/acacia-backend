package com.example.acacia.service;

import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.repository.ContributionPeriodRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContributionPeriodServiceImpl implements ContributionPeriodService {
    private final ContributionPeriodRepository contributionPeriodRepository;
    private final SaccoSetupRepository saccoSetupRepository;

    @Override
    public List<ContributionPeriod> getContributionPeriods() {
        try {
            return contributionPeriodRepository.findAll();
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

    }

    @Override
    public void addPeriod(LocalDate day) {
        try {
            SaccoSetups saccoSetups = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);

            DayOfWeek contributionDay = saccoSetups.getContributionDay();

            ContributionPeriod period = new ContributionPeriod();
            if(contributionDay.equals(day.getDayOfWeek())){
                period.setDate(day);
            }else {
                throw new RuntimeException("Invalid contribution day");
            }
            LocalTime deadlineTime = saccoSetups.getDeadlineTime();
            LocalDateTime deadline = day.plusDays(saccoSetups.getDaysToDeadline()).atTime(deadlineTime);
            period.setDeadline(deadline);
            period.setAmountRequired(saccoSetups.getContributionAmount());
            contributionPeriodRepository.save(period);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
