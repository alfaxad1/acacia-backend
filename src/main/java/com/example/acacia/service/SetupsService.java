package com.example.acacia.service;

import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.SaccoSetups;
import com.example.acacia.repository.SaccoSetupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SetupsService {
    private final SaccoSetupRepository setupRepository;
    public SaccoSetups getSetups() {
        return setupRepository.findByStatus(SetupStatus.ACTIVE);
    }

    public void editSetups(SaccoSetups incomingSetups) {
        SaccoSetups existingSetup = setupRepository.findByStatus(SetupStatus.ACTIVE);

        if (existingSetup == null) {
            throw new RuntimeException("No active setup found to update.");
        }

        existingSetup.setContributionDay(incomingSetups.getContributionDay());
        existingSetup.setContributionAmount(incomingSetups.getContributionAmount());
        existingSetup.setDaysToDeadline(incomingSetups.getDaysToDeadline());
        existingSetup.setDeadlineTime(incomingSetups.getDeadlineTime());
        existingSetup.setLatePaymentFineAmount(incomingSetups.getLatePaymentFineAmount());
        existingSetup.setMeetingAbsentFineAmount(incomingSetups.getMeetingAbsentFineAmount());
        existingSetup.setMeetingLateFineAmount(incomingSetups.getMeetingLateFineAmount());
        existingSetup.setLoanInterestRate(incomingSetups.getLoanInterestRate());
        existingSetup.setLoanPenaltyRate(incomingSetups.getLoanPenaltyRate());
        existingSetup.setLoanDuration(incomingSetups.getLoanDuration());
        existingSetup.setStatus(incomingSetups.getStatus());

        setupRepository.save(existingSetup);
    }
}
