package com.dtoan.project.fptassetmanagement.entity;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_usages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class AssetUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_from_id")
    private Room roomFrom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_to_id")
    private Room roomTo;

    @Column(name = "check_in_time", nullable = false)
    @Builder.Default
    private LocalDateTime checkInTime = LocalDateTime.now();

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(length = 500)
    private String purpose;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private UsageStatus status = UsageStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
