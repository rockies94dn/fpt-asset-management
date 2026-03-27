package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.*;
import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

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

        // Recent assets
        model.addAttribute("recentAssets",
                assetRepository.findRecentAssets(PageRequest.of(0, 8)));

        return "dashboard";
    }
}