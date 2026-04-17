package com.example.acacia.service;

import com.example.acacia.dto.ContributionDto;
import com.example.acacia.dto.ContributionPeriodDto;
import com.example.acacia.dto.MetaData;
import com.example.acacia.dto.Response;
import com.example.acacia.enums.ResponseStatusEnum;
import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.Contribution;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.repository.ContributionPeriodRepository;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContributionPeriodServiceImpl implements ContributionPeriodService {
    private final ContributionPeriodRepository contributionPeriodRepository;
    private final SaccoSetupRepository saccoSetupRepository;
    private final ContributionRepository contributionRepository;


    @Override
    public Response<List<ContributionPeriodDto>> getContributionPeriods(Pageable pageable) {
        Page<ContributionPeriod> periodPage = contributionPeriodRepository.findAll(pageable);
        List<ContributionPeriod> periods = periodPage.getContent();

        List<Contribution> allContributions = contributionRepository.findByPeriodIn(periods);

        Map<Long, List<Contribution>> contributionsByPeriodId = allContributions.stream()
                .collect(Collectors.groupingBy(c -> c.getPeriod().getId()));

        List<ContributionPeriodDto> periodDtos = periods.stream().map(p -> {
            List<Contribution> contributionsForThisPeriod = contributionsByPeriodId.getOrDefault(p.getId(), Collections.emptyList());

            List<ContributionDto> contributionDtos = contributionsForThisPeriod.stream()
                    .map(c -> ContributionDto.builder()
                            .id(c.getId())
                            .memberName(c.getMember().getFullName())
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

        MetaData metaData = MetaData.builder()
                .page(periodPage.getNumber())
                .limit(periodPage.getSize())
                .totalElements(periodPage.getTotalElements())
                .totalPages(periodPage.getTotalPages())
                .build();

        return Response.<List<ContributionPeriodDto>>builder()
                .status(ResponseStatusEnum.SUCCESS)
                .message("Contribution periods retrieved successfully")
                .data(periodDtos)
                .metaData(metaData)
                .build();
    }

    @Scheduled(cron = "0 0 0 * * FRI")
    @Transactional
    public void generateContributionPeriods() {
        try {
            SaccoSetups setup = saccoSetupRepository.findByStatus(SetupStatus.ACTIVE);
            if (setup == null) {
                log.warn("No active Sacco setup found. Skipping period generation.");
                return;
            }

            DayOfWeek targetDay = setup.getContributionDay();
            LocalDate today = LocalDate.now();

            LocalDate currentWeekFriday = today.with(TemporalAdjusters.previousOrSame(targetDay));

            LocalDate nextWeekFriday = currentWeekFriday.plusWeeks(1);

            savePeriodIfMissing(currentWeekFriday, setup);
            savePeriodIfMissing(nextWeekFriday, setup);

        } catch (Exception e) {
            log.error("Failed to generate contribution periods: {}", e.getMessage());
        }
    }

    private void savePeriodIfMissing(LocalDate date, SaccoSetups setup) {
        if (contributionPeriodRepository.existsByDate(date)) {
            log.info("Period for {} already exists, skipping.", date);
            return;
        }

        ContributionPeriod period = new ContributionPeriod();
        period.setDate(date);

        LocalDateTime deadline = date.plusDays(setup.getDaysToDeadline())
                .atTime(setup.getDeadlineTime());

        period.setDeadline(deadline);
        period.setAmountRequired(setup.getContributionAmount());

        contributionPeriodRepository.save(period);
        log.info("Successfully created new contribution period for {}", date);
    }
}
