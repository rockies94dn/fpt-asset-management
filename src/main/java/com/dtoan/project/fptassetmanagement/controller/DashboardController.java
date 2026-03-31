package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final int OVERDUE_MAINTENANCE_DAYS = 7;
    private static final List<MaintenanceStatus> OPEN_MAINTENANCE_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);

    private final AssetRepository assetRepository;
    private final AssetUsageRepository usageRepository;
    private final MaintenanceRequestRepository maintenanceRepository;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        // Stats
        model.addAttribute("totalAssets", assetRepository.countByIsActiveTrue());
        model.addAttribute("availableAssets", assetRepository.countByStatus(AssetStatus.AVAILABLE));
        model.addAttribute("inUseAssets", assetRepository.countByStatus(AssetStatus.IN_USE));
        model.addAttribute("brokenAssets", assetRepository.countByStatus(AssetStatus.BROKEN));
        model.addAttribute("maintenanceAssets", assetRepository.countByStatus(AssetStatus.MAINTENANCE));

        model.addAttribute("activeUsages", usageRepository.countByStatus(UsageStatus.ACTIVE));
        model.addAttribute("pendingMaintenance", maintenanceRepository.countByStatus(MaintenanceStatus.PENDING));
        model.addAttribute("overdueMaintenanceDays", OVERDUE_MAINTENANCE_DAYS);
        model.addAttribute("openMaintenanceRequests",
                maintenanceRepository.countByStatusIn(OPEN_MAINTENANCE_STATUSES));
        model.addAttribute("overdueMaintenanceRequests",
                maintenanceRepository.countByStatusInAndReportedAtBefore(
                        OPEN_MAINTENANCE_STATUSES,
                        LocalDateTime.now().minusDays(OVERDUE_MAINTENANCE_DAYS)
                ));
        model.addAttribute("attentionAssets",
                assetRepository.findAssetsNeedingAttention(
                        OPEN_MAINTENANCE_STATUSES,
                        PageRequest.of(0, 5)
                ));

        // Recent assets
        model.addAttribute("recentAssets",
                assetRepository.findRecentAssets(PageRequest.of(0, 8)));

        return "dashboard";
    }
}
