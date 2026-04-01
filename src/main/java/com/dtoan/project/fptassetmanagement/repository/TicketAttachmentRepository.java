package com.dtoan.project.fptassetmanagement.repository;

import com.dtoan.project.fptassetmanagement.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    Optional<TicketAttachment> findByIdAndTicketId(Long id, Long ticketId);
}
