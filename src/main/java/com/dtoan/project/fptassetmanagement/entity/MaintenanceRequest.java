package com.dtoan.project.fptassetmanagement.entity;

import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "issue_type", nullable = false, length = 50)
    private String issueType; // BROKEN, MAINTENANCE, UPGRADE

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 20)
    @Builder.Default
    private String priority = "NORMAL";

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PENDING;

    @Column(name = "reported_at")
    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 2000)
    private String resolutionNote;

    @Column(name = "estimated_cost", precision = 18, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 18, scale = 2)
    private BigDecimal actualCost;

    public String getPriorityDisplayName() {
        return switch (this.priority) {
            case "LOW" -> "Thấp";
            case "NORMAL" -> "Bình thường";
            case "HIGH" -> "Cao";
            case "URGENT" -> "Khẩn cấp";
            default -> this.priority;
        };
    }

    public String getPriorityBadgeClass() {
        return switch (this.priority) {
            case "LOW" -> "secondary";
            case "NORMAL" -> "info";
            case "HIGH" -> "warning";
            case "URGENT" -> "danger";
            default -> "secondary";
        };
    }

    public String getIssueTypeDisplayName() {
        return switch (this.issueType) {
            case "BROKEN" -> "Báo hỏng";
            case "MAINTENANCE" -> "Bảo trì định kỳ";
            case "UPGRADE" -> "Nâng cấp";
            default -> this.issueType;
        };
    }
}
