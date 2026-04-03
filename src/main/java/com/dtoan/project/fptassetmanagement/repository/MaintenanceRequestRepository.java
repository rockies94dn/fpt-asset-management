package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.enums.TicketCandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    @Query("SELECT m FROM MaintenanceRequest m WHERE " +
            "(:status IS NULL OR m.status = :status) " +
            "ORDER BY m.reportedAt DESC")
    Page<MaintenanceRequest> searchRequests(@Param("status") MaintenanceStatus status,
                                            Pageable pageable);

    @Query("SELECT m FROM MaintenanceRequest m WHERE " +
            "(LOWER(m.asset.name) LIKE LOWER(:keywordPattern)) AND " +
            "(:status IS NULL OR m.status = :status) " +
            "ORDER BY m.reportedAt DESC")
    Page<MaintenanceRequest> searchRequestsByKeyword(@Param("keywordPattern") String keywordPattern,
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MaintenanceRequest m WHERE m.id = :id")
    Optional<MaintenanceRequest> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            SELECT m
            FROM MaintenanceRequest m
            WHERE (:status IS NULL OR m.status = :status)
              AND (:reporterId IS NULL OR m.reportedBy.id = :reporterId)
              AND (
                    :viewerId IS NULL
                    OR m.assignedTo.id = :viewerId
                    OR EXISTS (
                        SELECT 1
                        FROM TicketCandidateAssignment tca
                        WHERE tca.ticket = m
                          AND tca.technician.id = :viewerId
                          AND tca.status = :candidateStatus
                    )
              )
            ORDER BY COALESCE(m.lastActivityAt, m.reportedAt) DESC
            """)
    Page<MaintenanceRequest> searchTicketApi(@Param("status") MaintenanceStatus status,
                                             @Param("viewerId") Long viewerId,
                                             @Param("reporterId") Long reporterId,
                                             @Param("candidateStatus") TicketCandidateStatus candidateStatus,
                                             Pageable pageable);

    @Query("""
            SELECT m
            FROM MaintenanceRequest m
            WHERE (
                    LOWER(m.asset.name) LIKE LOWER(:keywordPattern)
                    OR LOWER(m.ticketCode) LIKE LOWER(:keywordPattern)
                    OR LOWER(m.asset.qaCode) LIKE LOWER(:keywordPattern)
                  )
              AND (:status IS NULL OR m.status = :status)
              AND (:reporterId IS NULL OR m.reportedBy.id = :reporterId)
              AND (
                    :viewerId IS NULL
                    OR m.assignedTo.id = :viewerId
                    OR EXISTS (
                        SELECT 1
                        FROM TicketCandidateAssignment tca
                        WHERE tca.ticket = m
                          AND tca.technician.id = :viewerId
                          AND tca.status = :candidateStatus
                    )
              )
            ORDER BY COALESCE(m.lastActivityAt, m.reportedAt) DESC
            """)
    Page<MaintenanceRequest> searchTicketApiByKeyword(@Param("keywordPattern") String keywordPattern,
                                                      @Param("status") MaintenanceStatus status,
                                                      @Param("viewerId") Long viewerId,
                                                      @Param("reporterId") Long reporterId,
                                                      @Param("candidateStatus") TicketCandidateStatus candidateStatus,
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

    @Query("""
            SELECT m
            FROM MaintenanceRequest m
            WHERE m.status IN :statuses
              AND CURRENT_TIMESTAMP > m.slaDueAt
              AND (:reporterId IS NULL OR m.reportedBy.id = :reporterId)
              AND (
                    :viewerId IS NULL
                    OR m.assignedTo.id = :viewerId
                    OR EXISTS (
                        SELECT 1
                        FROM TicketCandidateAssignment tca
                        WHERE tca.ticket = m
                          AND tca.technician.id = :viewerId
                          AND tca.status = :candidateStatus
                    )
              )
            ORDER BY m.slaDueAt ASC
            """)
    List<MaintenanceRequest> findOverdueTicketsApi(@Param("statuses") Collection<MaintenanceStatus> statuses,
                                                   @Param("viewerId") Long viewerId,
                                                   @Param("reporterId") Long reporterId,
                                                   @Param("candidateStatus") TicketCandidateStatus candidateStatus);
}
