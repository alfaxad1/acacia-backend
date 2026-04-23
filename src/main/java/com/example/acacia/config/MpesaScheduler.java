package com.example.acacia.config;

import com.example.acacia.service.MpesaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpesaScheduler {
    private final MpesaService mpesaService;

    @Scheduled(cron = "0 0/30 * * * *")
    public void syncBalance() {
        try { mpesaService.checkAccountBalance(); }
        catch (Exception e) {
            log.error("Balance sync failed");
        }
    }
}
