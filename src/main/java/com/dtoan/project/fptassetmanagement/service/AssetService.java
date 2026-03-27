package com.dtoan.project.fptassetmanagement.service;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AssetService {
    Page<Asset> searchAssets(String keyword, AssetStatus status, Long categoryId, Long roomId, Pageable pageable);
    Optional<Asset> findById(Long id);
    Optional<Asset> findByQaCode(String qaCode);
    Asset save(Asset asset);
    void deleteById(Long id);
    long countByStatus(AssetStatus status);
    long countTotal();
    String generateQaCode(String categoryCode);
    String generateQRCode(String qaCode) throws Exception;
}
