package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.*;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private final MaintenanceRequestRepository maintenanceRepository;
    private final AssetRepository assetRepository;

    public MaintenanceRequest createRequest(Asset asset, User reportedBy,
                                            String issueType, String description, String priority) {
        MaintenanceRequest req = MaintenanceRequest.builder()
                .asset(asset)
                .reportedBy(reportedBy)
                .issueType(issueType)
                .description(description)
                .priority(priority)
                .status(MaintenanceStatus.PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        // Update asset status
        if ("BROKEN".equals(issueType)) {
            asset.setStatus(AssetStatus.BROKEN);
        } else {
            asset.setStatus(AssetStatus.MAINTENANCE);
        }
        assetRepository.save(asset);

        return maintenanceRepository.save(req);
    }

    public MaintenanceRequest resolve(Long requestId, String resolutionNote, User resolvedBy) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        req.setStatus(MaintenanceStatus.RESOLVED);
        req.setResolutionNote(resolutionNote);
        req.setResolvedAt(LocalDateTime.now());
        req.setAssignedTo(resolvedBy);

        // Restore asset to available
        req.getAsset().setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(req.getAsset());

        return maintenanceRepository.save(req);
    }

    public MaintenanceRequest updateStatus(Long requestId, MaintenanceStatus status) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        req.setStatus(status);
        return maintenanceRepository.save(req);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> searchRequests(String keyword, MaintenanceStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return maintenanceRepository.searchRequests(kw, status, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceRequest> findById(Long id) {
        return maintenanceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(MaintenanceStatus status) {
        return maintenanceRepository.countByStatus(status);
    }
}
