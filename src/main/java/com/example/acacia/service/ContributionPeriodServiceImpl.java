package com.example.acacia.service;

import com.example.acacia.dto.ContributionDto;
import com.example.acacia.dto.ContributionPeriodDto;
import com.example.acacia.enums.MemberStatus;
import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.Contribution;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.repository.ContributionPeriodRepository;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContributionPeriodServiceImpl implements ContributionPeriodService {
    private final ContributionPeriodRepository contributionPeriodRepository;
    private final SaccoSetupRepository saccoSetupRepository;
    private final ContributionRepository contributionRepository;
    private final MemberRepository memberRepository;
    private final SaccoSetupRepository setupRepository;

    @Override
    public List<ContributionPeriodDto> getContributionPeriods() {

//        Long activeMembers = memberRepository.countActive(MemberStatus.ACTIVE);
//        SaccoSetups saccoSetups = setupRepository.findByStatus(SetupStatus.ACTIVE);
//        BigDecimal contributionAmount = saccoSetups.getContributionAmount();



        // 1. Fetch all periods
        List<ContributionPeriod> periods = contributionPeriodRepository.findAll();

        // 2. Fetch all contributions for these periods in ONE query
        // This assumes you add a findByPeriodIn method to your ContributionRepository
        List<Contribution> allContributions = contributionRepository.findByPeriodIn(periods);

        // 3. Group contributions by Period ID for quick lookup
        Map<Long, List<Contribution>> contributionsByPeriodId = allContributions.stream()
                .collect(Collectors.groupingBy(c -> c.getPeriod().getId()));

// 2. When building the Period DTO, map those specific entities to DTOs
        return periods.stream().map(p -> {
            List<Contribution> contributionsForThisPeriod = contributionsByPeriodId.getOrDefault(p.getId(), Collections.emptyList());

            List<ContributionDto> contributionDtos = contributionsForThisPeriod.stream()
                    .map(c -> ContributionDto.builder()
                            .id(c.getId())
                            .amount(c.getAmount())
                            .paymentDate(c.getPaymentDate())
                            .isLate(c.isLate())
                            .build())
                    .collect(Collectors.toList());

            return ContributionPeriodDto.builder()
                    .id(p.getId())
                    .date(p.getDate())
                    .deadline(p.getDeadline())
                    .amountRequired(p.getAmountRequired())
                    .contributions(contributionDtos)
                    .build();
        }).collect(Collectors.toList());
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
