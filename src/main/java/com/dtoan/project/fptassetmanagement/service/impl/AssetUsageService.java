package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.AssetUsage;
import com.dtoan.project.fptassetmanagement.entity.Room;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.dtoan.project.fptassetmanagement.repository.AssetUsageRepository;
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
public class AssetUsageService {

    private final AssetUsageRepository usageRepository;
    private final AssetRepository assetRepository;
    private final RoomService roomService;

    public AssetUsage checkIn(Asset asset, User user, Room roomTo, String purpose) {
        if (!asset.canBeUsed()) {
            throw new IllegalStateException("Thiết bị không thể sử dụng. Trạng thái: " + asset.getStatus().getDisplayName());
        }

        AssetUsage usage = AssetUsage.builder()
                .asset(asset)
                .user(user)
                .roomFrom(asset.getRoom())
                .roomTo(roomTo)
                .checkInTime(LocalDateTime.now())
                .purpose(purpose)
                .status(UsageStatus.ACTIVE)
                .build();

        asset.setStatus(AssetStatus.IN_USE);
        if (roomTo != null) asset.setRoom(roomTo);
        assetRepository.save(asset);

        return usageRepository.save(usage);
    }

    public AssetUsage checkOut(Long usageId, String note) {
        AssetUsage usage = usageRepository.findById(usageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu sử dụng"));

        usage.setCheckOutTime(LocalDateTime.now());
        usage.setNote(note);
        usage.setStatus(UsageStatus.COMPLETED);

        Asset asset = usage.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        Room storeRoom = roomService.getOrCreateStoreRoom();
        asset.setRoom(storeRoom);
        assetRepository.save(asset);

        return usageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public Page<AssetUsage> searchUsages(String keyword, UsageStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return usageRepository.searchUsages(kw, status, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<AssetUsage> findActiveUsageByAsset(Long assetId) {
        return usageRepository.findByAssetIdAndStatus(assetId, UsageStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Optional<AssetUsage> findById(Long id) {
        return usageRepository.findById(id);
    }
}

