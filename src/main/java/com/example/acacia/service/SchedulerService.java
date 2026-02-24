package com.example.acacia.service;

import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.model.Member;
import com.example.acacia.repository.ContributionRepository;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.repository.SaccoSetupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService {
    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final ContributionRepository contributionRepository;
    private final SaccoSetupRepository setupRepository;

    /**
     * Runs every Friday at 6:00 AM
     * Cron: 0 0 6 ? * FRI
     */
    @Scheduled(cron = "0 0 6 ? * FRI")
    public void sendFridayReminders() {
        List<Member> allMembers = memberRepository.findAll();
        String subject = "Payment Reminder: Friday Contribution";
        String content = "Hello, this is a reminder to make your scheduled contribution today.";

        for (Member member : allMembers) {
            try {
                emailService.sendMail(member.getEmail(), subject, content);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * Runs every Saturday at 6:00 PM (18:00)
     * Cron: 0 0 18 ? * SAT
     */
    @Scheduled(cron = "0 0 18 ? * SAT")
    public void checkLatePaymentsAndPenalize() {
        LocalDate contributionDate = LocalDate.now().minusDays(1);

        List<Long> ids = contributionRepository.findPaidMemberIds(contributionDate);

        BigDecimal penaltyAmount = setupRepository.findByStatus(SetupStatus.ACTIVE).getLatePaymentFineAmount();

        List<Member>  defaulters = memberRepository.findByIdNotIn(ids);

        String subject = "Late Payment Penalty Notice";

        for (Member member : defaulters) {
            String content = "Our records show you missed your contribution on " + contributionDate
                    + ". A penalty of Ksh."+ penaltyAmount + "has been applied to your account.";
            try {
                emailService.sendMail(member.getEmail(), subject, content);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
