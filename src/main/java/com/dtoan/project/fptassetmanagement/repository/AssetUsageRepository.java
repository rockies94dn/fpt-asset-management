package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.AssetUsage;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetUsageRepository extends JpaRepository<AssetUsage, Long> {

    Optional<AssetUsage> findByAssetIdAndStatus(Long assetId, UsageStatus status);

    @Query("SELECT u FROM AssetUsage u WHERE " +
            "(:status IS NULL OR u.status = :status) " +
            "ORDER BY u.checkInTime DESC")
    Page<AssetUsage> searchUsages(@Param("status") UsageStatus status,
                                  Pageable pageable);

    @Query("SELECT u FROM AssetUsage u WHERE " +
            "(LOWER(u.asset.name) LIKE LOWER(:keywordPattern) OR " +
            "LOWER(u.user.fullName) LIKE LOWER(:keywordPattern)) AND " +
            "(:status IS NULL OR u.status = :status) " +
            "ORDER BY u.checkInTime DESC")
    Page<AssetUsage> searchUsagesByKeyword(@Param("keywordPattern") String keywordPattern,
                                           @Param("status") UsageStatus status,
                                           Pageable pageable);

    @Query("SELECT u FROM AssetUsage u WHERE u.asset.id = :assetId ORDER BY u.checkInTime DESC")
    List<AssetUsage> findByAssetIdOrderByCheckInTimeDesc(@Param("assetId") Long assetId);

    List<AssetUsage> findTop10ByOrderByCheckInTimeDesc();

    List<AssetUsage> findTop10ByCheckOutTimeIsNotNullOrderByCheckOutTimeDesc();

    long countByStatus(UsageStatus status);
}
