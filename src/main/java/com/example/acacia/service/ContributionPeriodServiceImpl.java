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
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
