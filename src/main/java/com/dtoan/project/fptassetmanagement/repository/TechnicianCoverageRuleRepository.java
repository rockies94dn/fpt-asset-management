package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.TechnicianCoverageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicianCoverageRuleRepository extends JpaRepository<TechnicianCoverageRule, Long> {
    List<TechnicianCoverageRule> findByIsActiveTrueOrderBySortOrderAscIdAsc();
}
