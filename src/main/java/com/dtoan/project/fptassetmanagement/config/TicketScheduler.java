package com.dtoan.project.fptassetmanagement.config;

import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketScheduler {

    private final MaintenanceService maintenanceService;

    @Scheduled(fixedDelay = 300000)
    public void flagOverdueTickets() {
        maintenanceService.markOverdueTickets();
    }
}
