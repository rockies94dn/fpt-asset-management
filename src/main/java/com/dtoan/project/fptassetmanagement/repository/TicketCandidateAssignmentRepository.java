package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.TicketCandidateAssignment;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.TicketCandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketCandidateAssignmentRepository extends JpaRepository<TicketCandidateAssignment, Long> {

    List<TicketCandidateAssignment> findByTicketIdOrderByCreatedAtAscIdAsc(Long ticketId);

    List<TicketCandidateAssignment> findByTicketIdAndStatusOrderByCreatedAtAscIdAsc(Long ticketId,
                                                                                    TicketCandidateStatus status);

    Optional<TicketCandidateAssignment> findByTicketIdAndTechnicianId(Long ticketId, Long technicianId);

    boolean existsByTicketIdAndTechnicianIdAndStatus(Long ticketId, Long technicianId, TicketCandidateStatus status);

    @Query("""
            SELECT tca.technician
            FROM TicketCandidateAssignment tca
            WHERE tca.ticket.id = :ticketId
              AND tca.status = :status
            ORDER BY tca.createdAt ASC, tca.id ASC
            """)
    List<User> findTechniciansByTicketIdAndStatus(Long ticketId, TicketCandidateStatus status);
}
