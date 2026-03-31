package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetServiceImpl implements AssetService {

    private static final List<MaintenanceStatus> OPEN_MAINTENANCE_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);

    private final AssetRepository assetRepository;
    private final QRCodeUtil qrCodeUtil;

    @Override
    @Transactional(readOnly = true)
    public Page<Asset> searchAssets(String keyword, AssetStatus status, Long categoryId, Long roomId,
                                    boolean attentionOnly, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return assetRepository.searchAssets(
                kw,
                status,
                categoryId,
                roomId,
                attentionOnly,
                OPEN_MAINTENANCE_STATUSES,
                pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findById(Long id) {
        return assetRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> findByQaCode(String qaCode) {
        return assetRepository.findByQaCode(qaCode);
    }

    @Override
    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }

    @Override
    public void deleteById(Long id) {
        assetRepository.findById(id).ifPresent(asset -> {
            asset.setIsActive(false);
            assetRepository.save(asset);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(AssetStatus status) {
        return assetRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return assetRepository.countByIsActiveTrue();
    }

    @Override
    public String generateQaCode(String categoryCode) {
        String prefix = "FPT-" + categoryCode.toUpperCase();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String code = prefix + "-" + timestamp;
        // Ensure uniqueness
        int suffix = 1;
        String candidate = code;
        while (assetRepository.existsByQaCode(candidate)) {
            candidate = code + "-" + suffix++;
        }
        return candidate;
    }

    @Override
    public String generateQRCode(String qaCode) throws Exception {
        String content = qrCodeUtil.buildAssetQRContent(qaCode);
        return qrCodeUtil.generateQRCodeWithLabel(content, qaCode);
    }
}
