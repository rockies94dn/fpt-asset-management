package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
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

    @Query("SELECT a FROM Asset a WHERE a.isActive = true AND " +
            "(:keyword IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.qaCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
            "(:roomId IS NULL OR a.room.id = :roomId)")
    Page<Asset> searchAssets(
            @Param("keyword") String keyword,
            @Param("status") AssetStatus status,
            @Param("categoryId") Long categoryId,
            @Param("roomId") Long roomId,
            Pageable pageable);

    long countByStatus(AssetStatus status);

    long countByIsActiveTrue();

    @Query("SELECT a FROM Asset a WHERE a.isActive = true ORDER BY a.createdAt DESC")
    List<Asset> findRecentAssets(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.room.id = :roomId AND a.isActive = true")
    List<Asset> findByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status AND a.isActive = true")
    long countActiveByStatus(@Param("status") AssetStatus status);
}