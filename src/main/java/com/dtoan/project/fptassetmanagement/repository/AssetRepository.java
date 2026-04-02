package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByQaCode(String qaCode);

    boolean existsByQaCode(String qaCode);

    List<Asset> findByIsActiveTrueOrderByNameAsc();

    @Query("SELECT a FROM Asset a WHERE a.isActive = true AND " +
            "(:keyword IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.qaCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
            "(:roomId IS NULL OR a.room.id = :roomId) AND " +
            "(:attentionOnly = false OR " +
            "EXISTS (SELECT 1 FROM MaintenanceRequest m WHERE m.asset = a AND m.status IN :openStatuses))")
    Page<Asset> searchAssets(
            @Param("keyword") String keyword,
            @Param("status") AssetStatus status,
            @Param("categoryId") Long categoryId,
            @Param("roomId") Long roomId,
            @Param("attentionOnly") boolean attentionOnly,
            @Param("openStatuses") List<MaintenanceStatus> openStatuses,
            Pageable pageable);

    long countByStatus(AssetStatus status);

    long countByIsActiveTrue();

    @Query("SELECT a FROM Asset a WHERE a.isActive = true ORDER BY a.createdAt DESC")
    List<Asset> findRecentAssets(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.room.id = :roomId AND a.isActive = true")
    List<Asset> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status AND a.isActive = true")
    long countActiveByStatus(@Param("status") AssetStatus status);

    @Query("SELECT a FROM Asset a WHERE a.isActive = true AND " +
            "EXISTS (SELECT 1 FROM MaintenanceRequest m WHERE m.asset = a AND m.status IN :openStatuses) " +
            "ORDER BY a.updatedAt DESC, a.createdAt DESC")
    List<Asset> findAssetsNeedingAttention(@Param("openStatuses") List<MaintenanceStatus> openStatuses,
                                           Pageable pageable);
}
