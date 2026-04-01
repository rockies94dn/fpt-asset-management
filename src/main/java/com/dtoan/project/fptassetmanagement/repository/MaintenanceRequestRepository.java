package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    @Query("SELECT m FROM MaintenanceRequest m WHERE " +
            "(:keyword IS NULL OR LOWER(m.asset.name) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND " +
            "(:status IS NULL OR m.status = :status) " +
            "ORDER BY m.reportedAt DESC")
    Page<MaintenanceRequest> searchRequests(@Param("keyword") String keyword,
                                            @Param("status") MaintenanceStatus status,
                                            Pageable pageable);

    long countByStatus(MaintenanceStatus status);

    long countByStatusIn(Collection<MaintenanceStatus> statuses);

    long countByStatusInAndReportedAtBefore(Collection<MaintenanceStatus> statuses, LocalDateTime before);

    Optional<MaintenanceRequest> findFirstByAssetIdAndStatusInOrderByReportedAtDesc(Long assetId,
                                                                                    Collection<MaintenanceStatus> statuses);

    List<MaintenanceRequest> findByAssetIdOrderByReportedAtDesc(Long assetId);

    List<MaintenanceRequest> findTop10ByOrderByReportedAtDesc();

    List<MaintenanceRequest> findTop10ByStatusInAndReportedAtBeforeOrderByReportedAtDesc(Collection<MaintenanceStatus> statuses,
                                                                                          LocalDateTime before);

    List<MaintenanceRequest> findTop10ByAssignedToIdOrderByLastActivityAtDesc(Long assignedToId);

    long countByAssignedToIdAndStatusIn(Long assignedToId, Collection<MaintenanceStatus> statuses);

    List<MaintenanceRequest> findByStatusIn(Collection<MaintenanceStatus> statuses);

    @Query("SELECT m FROM MaintenanceRequest m WHERE " +
            "(:keyword IS NULL OR LOWER(m.asset.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.ticketCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.asset.qaCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR m.status = :status) AND " +
            "(:assigneeId IS NULL OR m.assignedTo.id = :assigneeId) AND " +
            "(:reporterId IS NULL OR m.reportedBy.id = :reporterId) " +
            "ORDER BY COALESCE(m.lastActivityAt, m.reportedAt) DESC")
    Page<MaintenanceRequest> searchTicketApi(@Param("keyword") String keyword,
                                             @Param("status") MaintenanceStatus status,
                                             @Param("assigneeId") Long assigneeId,
                                             @Param("reporterId") Long reporterId,
                                             Pageable pageable);

    @Query("SELECT m.asset.id, COUNT(m) FROM MaintenanceRequest m " +
            "WHERE m.asset.id IN :assetIds AND m.status IN :statuses GROUP BY m.asset.id")
    List<Object[]> countOpenRequestsByAssetIds(@Param("assetIds") Collection<Long> assetIds,
                                               @Param("statuses") Collection<MaintenanceStatus> statuses);

    @Query("SELECT DISTINCT m.asset.id FROM MaintenanceRequest m " +
            "WHERE m.asset.id IN :assetIds AND m.status IN :statuses AND m.reportedAt <= :before")
    List<Long> findAssetIdsWithOverdueOpenRequests(@Param("assetIds") Collection<Long> assetIds,
                                                   @Param("statuses") Collection<MaintenanceStatus> statuses,
                                                   @Param("before") LocalDateTime before);
}
