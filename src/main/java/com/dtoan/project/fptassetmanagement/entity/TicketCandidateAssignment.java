package com.dtoan.project.fptassetmanagement.entity;

import com.dtoan.project.fptassetmanagement.enums.TicketCandidateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_candidate_assignments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_candidate_assignment_ticket_technician",
                        columnNames = {"ticket_id", "technician_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCandidateAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MaintenanceRequest ticket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketCandidateStatus status = TicketCandidateStatus.PENDING;

    @Column(name = "assignment_source", nullable = false, length = 30)
    private String assignmentSource;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
}
