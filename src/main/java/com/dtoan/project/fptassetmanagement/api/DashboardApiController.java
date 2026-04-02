package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.dtoan.project.fptassetmanagement.repository.AssetUsageRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private static final List<MaintenanceStatus> OPEN_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);

    private final AssetRepository assetRepository;
    private final AssetUsageRepository usageRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final MaintenanceService maintenanceService;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;

    @GetMapping
    public ApiDtos.DashboardResponse dashboard(Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        ApiDtos.DashboardSummaryDto summary = new ApiDtos.DashboardSummaryDto(
                assetRepository.countByIsActiveTrue(),
                assetRepository.countActiveByStatus(AssetStatus.AVAILABLE),
                assetRepository.countActiveByStatus(AssetStatus.IN_USE),
                assetRepository.countActiveByStatus(AssetStatus.BROKEN),
                assetRepository.countActiveByStatus(AssetStatus.MAINTENANCE),
                assetRepository.countActiveByStatus(AssetStatus.LOST),
                usageRepository.countByStatus(UsageStatus.ACTIVE),
                maintenanceRepository.countByStatus(MaintenanceStatus.PENDING),
                maintenanceRepository.countByStatusIn(OPEN_STATUSES),
                maintenanceRepository.countByStatusInAndReportedAtBefore(
                        OPEN_STATUSES,
                        LocalDateTime.now().minusHours(4)
                )
        );

        return new ApiDtos.DashboardResponse(
                summary,
                assetRepository.findRecentAssets(PageRequest.of(0, 8)).stream().map(apiMapper::toAssetDto).toList(),
                assetRepository.findAssetsNeedingAttention(OPEN_STATUSES, PageRequest.of(0, 5))
                        .stream().map(apiMapper::toAssetDto).toList(),
                (user.isAdmin() ? maintenanceService.searchTickets(null, null, null, null, PageRequest.of(0, 5))
                        : maintenanceService.searchTickets(
                                null,
                                null,
                                currentUserService.hasRole(user, "MAINTENANCE") ? user.getId() : null,
                                currentUserService.hasRole(user, "STAFF") ? user.getId() : null,
                                PageRequest.of(0, 5)
                        )).getContent().stream().map(apiMapper::toTicketDto).toList()
        );
    }
}
