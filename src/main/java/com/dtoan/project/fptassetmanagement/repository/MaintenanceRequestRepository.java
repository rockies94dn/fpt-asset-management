package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}