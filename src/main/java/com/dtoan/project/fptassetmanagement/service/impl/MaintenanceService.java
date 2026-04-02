package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.*;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private static final List<MaintenanceStatus> OPEN_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);

    private final MaintenanceRequestRepository maintenanceRepository;
    private final AssetRepository assetRepository;
    private final TechnicianCoverageRuleRepository coverageRuleRepository;
    private final UserRepository userRepository;
    private final TicketRealtimeService ticketRealtimeService;

    public MaintenanceRequest createRequest(Asset asset, User reportedBy,
                                            String issueType, String description, String priority) {
        LocalDateTime now = LocalDateTime.now();
        MaintenanceRequest req = MaintenanceRequest.builder()
                .asset(asset)
                .reportedBy(reportedBy)
                .issueType(issueType)
                .description(description)
                .priority(priority)
                .ticketCode(generateTicketCode())
                .reportedRoomSnapshot(asset.getRoom() != null ? asset.getRoom().getName() : null)
                .status(MaintenanceStatus.PENDING)
                .reportedAt(now)
                .lastActivityAt(now)
                .slaDueAt(calculateSlaDueAt(priority, now))
                .build();

        pickAssignee(asset, issueType).ifPresent(assignee -> {
            req.setAssignedTo(assignee);
            req.setAssignmentSource("AUTO");
        });

        // Update asset status
        if ("BROKEN".equals(issueType)) {
            asset.setStatus(AssetStatus.BROKEN);
        } else {
            asset.setStatus(AssetStatus.MAINTENANCE);
        }
        assetRepository.save(asset);

        MaintenanceRequest saved = maintenanceRepository.save(req);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_CREATED",
                reportedBy.getFullName() + " đã tạo ticket " + saved.getTicketCode() + " cho " + asset.getName() + ".",
                recipients(saved)
        );
        return saved;
    }

    public MaintenanceRequest resolve(Long requestId, String resolutionNote, User resolvedBy) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        if (!canResolveTicket(req, resolvedBy)) {
            throw new IllegalStateException("Chỉ quản trị viên hoặc kỹ thuật viên được giao mới có thể hoàn tất ticket đang xử lý.");
        }

        req.setStatus(MaintenanceStatus.RESOLVED);
        req.setResolutionNote(resolutionNote);
        req.setResolvedAt(LocalDateTime.now());
        req.setAssignedTo(resolvedBy);
        req.setLastActivityAt(LocalDateTime.now());

        // Restore asset to available
        req.getAsset().setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(req.getAsset());

        MaintenanceRequest saved = maintenanceRepository.save(req);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_RESOLVED",
                resolvedBy.getFullName() + " đã giải quyết ticket " + saved.getTicketCode() + ".",
                recipients(saved)
        );
        return saved;
    }

    public MaintenanceRequest assign(Long requestId, User assignee) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        req.setAssignedTo(assignee);
        req.setAssignmentSource("MANUAL");
        req.setLastActivityAt(LocalDateTime.now());

        MaintenanceRequest saved = maintenanceRepository.save(req);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_ASSIGNED",
                "Ticket " + saved.getTicketCode() + " đã được giao cho " + assignee.getFullName() + ".",
                recipients(saved)
        );
        return saved;
    }

    public MaintenanceRequest updateStatus(Long requestId, MaintenanceStatus status) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        validateStatusTransition(req, status, null);
        req.setStatus(status);
        req.setLastActivityAt(LocalDateTime.now());
        if (status == MaintenanceStatus.RESOLVED && req.getResolvedAt() == null) {
            req.setResolvedAt(LocalDateTime.now());
            req.getAsset().setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(req.getAsset());
        }
        MaintenanceRequest saved = maintenanceRepository.save(req);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_STATUS_CHANGED",
                "Ticket " + saved.getTicketCode() + " đã chuyển sang " + status.getDisplayName() + ".",
                recipients(saved)
        );
        return saved;
    }

    public MaintenanceRequest updateStatus(Long requestId, MaintenanceStatus status, User actor) {
        MaintenanceRequest req = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        validateStatusTransition(req, status, actor);
        req.setStatus(status);
        req.setLastActivityAt(LocalDateTime.now());
        if (status == MaintenanceStatus.RESOLVED && req.getResolvedAt() == null) {
            req.setResolvedAt(LocalDateTime.now());
            req.getAsset().setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(req.getAsset());
        }
        MaintenanceRequest saved = maintenanceRepository.save(req);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_STATUS_CHANGED",
                "Ticket " + saved.getTicketCode() + " đã chuyển sang " + status.getDisplayName() + ".",
                recipients(saved)
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> searchRequests(String keyword, MaintenanceStatus status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return maintenanceRepository.searchRequests(kw, status, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<MaintenanceRequest> findById(Long id) {
        return maintenanceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long countByStatus(MaintenanceStatus status) {
        return maintenanceRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceRequest> searchTickets(String keyword, MaintenanceStatus status, Long assigneeId, Long reporterId, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return maintenanceRepository.searchTicketApi(kw, status, assigneeId, reporterId, pageable);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> findAssignedTickets(Long userId) {
        return maintenanceRepository.findTop10ByAssignedToIdOrderByLastActivityAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceStatus> getOpenStatuses() {
        return OPEN_STATUSES;
    }

    @Transactional(readOnly = true)
    public boolean canAccessTicket(MaintenanceRequest ticket, User user) {
        if (ticket == null || user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (ticket.getReportedBy() != null && user.getId().equals(ticket.getReportedBy().getId())) {
            return true;
        }
        return ticket.getAssignedTo() != null && user.getId().equals(ticket.getAssignedTo().getId());
    }

    public boolean canAcceptTicket(MaintenanceRequest ticket, User user) {
        return ticket != null
                && user != null
                && currentAssignee(ticket, user)
                && MaintenanceStatus.PENDING.equals(ticket.getStatus());
    }

    public boolean canResolveTicket(MaintenanceRequest ticket, User user) {
        if (ticket == null || user == null || !MaintenanceStatus.IN_PROGRESS.equals(ticket.getStatus())) {
            return false;
        }
        return user.isAdmin() || currentAssignee(ticket, user);
    }

    public boolean canCancelTicket(MaintenanceRequest ticket, User user) {
        return ticket != null
                && user != null
                && user.isAdmin()
                && (MaintenanceStatus.PENDING.equals(ticket.getStatus()) || MaintenanceStatus.IN_PROGRESS.equals(ticket.getStatus()));
    }

    public MaintenanceRequest save(MaintenanceRequest request) {
        return maintenanceRepository.save(request);
    }

    public List<MaintenanceRequest> markOverdueTickets() {
        List<MaintenanceRequest> overdue = maintenanceRepository.findByStatusIn(OPEN_STATUSES);
        for (MaintenanceRequest ticket : overdue) {
            if (ticket.getSlaDueAt() != null
                    && LocalDateTime.now().isAfter(ticket.getSlaDueAt())
                    && ticket.getSlaBreachedAt() == null) {
                ticket.setSlaBreachedAt(LocalDateTime.now());
                maintenanceRepository.save(ticket);
                ticketRealtimeService.broadcastTicketChanged(
                        ticket,
                        "TICKET_OVERDUE",
                        "Ticket " + ticket.getTicketCode() + " đã quá hạn SLA.",
                        recipients(ticket)
                );
            }
        }
        return overdue;
    }

    private LocalDateTime calculateSlaDueAt(String priority, LocalDateTime now) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return now.plusHours(4);
        }
        if ("LOW".equalsIgnoreCase(priority)) {
            return now.plusHours(24);
        }
        return now.plusHours(8);
    }

    private Optional<User> pickAssignee(Asset asset, String issueType) {
        List<TechnicianCoverageRule> rules = coverageRuleRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc();
        Optional<User> fromRules = rules.stream()
                .filter(rule -> rule.getTechnician() != null && Boolean.TRUE.equals(rule.getTechnician().getIsActive()))
                .filter(rule -> matches(rule, asset, issueType))
                .sorted(Comparator
                        .comparingInt((TechnicianCoverageRule rule) -> matchRank(rule, asset, issueType))
                        .thenComparing(TechnicianCoverageRule::getSortOrder)
                        .thenComparing(TechnicianCoverageRule::getId))
                .map(TechnicianCoverageRule::getTechnician)
                .findFirst();

        if (fromRules.isPresent()) {
            return fromRules;
        }

        return userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("MAINTENANCE").stream()
                .min(Comparator.comparingLong(user ->
                        maintenanceRepository.countByAssignedToIdAndStatusIn(user.getId(), OPEN_STATUSES)));
    }

    private boolean matches(TechnicianCoverageRule rule, Asset asset, String issueType) {
        boolean roomMatches = rule.getRoom() == null
                || (asset.getRoom() != null && rule.getRoom().getId().equals(asset.getRoom().getId()));
        boolean categoryMatches = rule.getCategory() == null
                || (asset.getCategory() != null && rule.getCategory().getId().equals(asset.getCategory().getId()));
        boolean issueMatches = rule.getIssueType() == null
                || rule.getIssueType().isBlank()
                || rule.getIssueType().equalsIgnoreCase(issueType);
        return roomMatches && categoryMatches && issueMatches;
    }

    private int matchRank(TechnicianCoverageRule rule, Asset asset, String issueType) {
        boolean room = rule.getRoom() != null && asset.getRoom() != null && rule.getRoom().getId().equals(asset.getRoom().getId());
        boolean category = rule.getCategory() != null && asset.getCategory() != null && rule.getCategory().getId().equals(asset.getCategory().getId());
        boolean issue = rule.getIssueType() != null && !rule.getIssueType().isBlank() && rule.getIssueType().equalsIgnoreCase(issueType);
        if (room && category && issue) {
            return 1;
        }
        if (category && issue && rule.getRoom() == null) {
            return 2;
        }
        if (room && category && (rule.getIssueType() == null || rule.getIssueType().isBlank())) {
            return 3;
        }
        if (category && rule.getRoom() == null && (rule.getIssueType() == null || rule.getIssueType().isBlank())) {
            return 4;
        }
        return 5;
    }

    private String generateTicketCode() {
        return "TKT-" + System.currentTimeMillis();
    }

    private void validateStatusTransition(MaintenanceRequest ticket, MaintenanceStatus targetStatus, User actor) {
        if (targetStatus == null) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        }
        if (targetStatus == MaintenanceStatus.PENDING) {
            throw new IllegalStateException("Không thể chuyển ticket quay lại trạng thái chờ tiếp nhận.");
        }
        if (targetStatus == MaintenanceStatus.IN_PROGRESS) {
            if (actor == null || !canAcceptTicket(ticket, actor)) {
                throw new IllegalStateException("Chỉ kỹ thuật viên được giao mới có thể nhận việc.");
            }
            return;
        }
        if (targetStatus == MaintenanceStatus.RESOLVED) {
            if (actor == null || !canResolveTicket(ticket, actor)) {
                throw new IllegalStateException("Chỉ quản trị viên hoặc kỹ thuật viên được giao mới có thể đánh dấu đã giải quyết.");
            }
            return;
        }
        if (targetStatus == MaintenanceStatus.CANCELLED) {
            if (actor == null || !canCancelTicket(ticket, actor)) {
                throw new IllegalStateException("Chỉ quản trị viên mới có thể hủy ticket còn mở.");
            }
            return;
        }
        throw new IllegalStateException("Không hỗ trợ chuyển trạng thái này.");
    }

    private boolean currentAssignee(MaintenanceRequest ticket, User user) {
        return ticket.getAssignedTo() != null
                && user.getId() != null
                && user.getId().equals(ticket.getAssignedTo().getId());
    }

    private Collection<User> recipients(MaintenanceRequest ticket) {
        List<User> users = new ArrayList<>();
        users.add(ticket.getReportedBy());
        users.add(ticket.getAssignedTo());
        users.addAll(userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("ADMIN"));
        return ticketRealtimeService.distinctUsers(users);
    }
}
