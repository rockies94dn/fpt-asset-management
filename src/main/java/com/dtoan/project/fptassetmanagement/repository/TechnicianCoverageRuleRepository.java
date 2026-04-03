package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.TechnicianCoverageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianCoverageRuleRepository extends JpaRepository<TechnicianCoverageRule, Long> {
    List<TechnicianCoverageRule> findByIsActiveTrueOrderBySortOrderAscIdAsc();

    List<TechnicianCoverageRule> findByTechnicianIdAndIsActiveTrueOrderBySortOrderAscIdAsc(Long technicianId);

    @Query("""
            SELECT COUNT(t) > 0
            FROM TechnicianCoverageRule t
            WHERE t.isActive = true
              AND t.technician.id = :technicianId
              AND ((:categoryId IS NULL AND t.category IS NULL) OR t.category.id = :categoryId)
              AND ((:roomId IS NULL AND t.room IS NULL) OR t.room.id = :roomId)
              AND (
                    (:issueType = '' AND (t.issueType IS NULL OR TRIM(t.issueType) = ''))
                    OR UPPER(COALESCE(t.issueType, '')) = :issueType
              )
            """)
    boolean existsActiveDuplicate(Long technicianId, Long categoryId, Long roomId, String issueType);
}
