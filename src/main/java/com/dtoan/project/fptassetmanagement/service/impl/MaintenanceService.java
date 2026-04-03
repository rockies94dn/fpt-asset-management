package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.TechnicianCoverageRule;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.TicketCandidateAssignment;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.enums.TicketCandidateStatus;
import com.dtoan.project.fptassetmanagement.exception.TicketAlreadyClaimedException;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.repository.TechnicianCoverageRuleRepository;
import com.dtoan.project.fptassetmanagement.repository.TicketCandidateAssignmentRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
    private final TicketCandidateAssignmentRepository candidateAssignmentRepository;
    private final UserRepository userRepository;
    private final TicketRealtimeService ticketRealtimeService;

    public MaintenanceRequest createRequest(Asset asset, User reportedBy,
                                            String issueType, String description, String priority) {
        LocalDateTime now = LocalDateTime.now();
        MaintenanceRequest request = MaintenanceRequest.builder()
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

        if ("BROKEN".equals(issueType)) {
            asset.setStatus(AssetStatus.BROKEN);
        } else {
            asset.setStatus(AssetStatus.MAINTENANCE);
        }
        assetRepository.save(asset);

        MaintenanceRequest saved = maintenanceRepository.save(request);
        List<TicketCandidateAssignment> candidates = createCandidateAssignments(saved, asset, issueType);
        if (!candidates.isEmpty()) {
            saved.setAssignmentSource(candidates.getFirst().getAssignmentSource());
            saved = maintenanceRepository.save(saved);
        }

        Collection<User> visibleRecipients = visibleRecipients(saved);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_CREATED",
                reportedBy.getFullName() + " đã tạo ticket " + saved.getTicketCode() + " cho " + asset.getName() + ".",
                visibleRecipients,
                visibleRecipients
        );
        return saved;
    }

    public MaintenanceRequest claim(Long requestId, User actor) {
        MaintenanceRequest request = maintenanceRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));

        if (!MaintenanceStatus.PENDING.equals(request.getStatus()) || request.getAssignedTo() != null) {
            throw new TicketAlreadyClaimedException("Ticket đã được kỹ thuật viên khác nhận.");
        }

        TicketCandidateAssignment actorCandidate = candidateAssignmentRepository
                .findByTicketIdAndTechnicianId(requestId, actor.getId())
                .orElseThrow(() -> new IllegalStateException("Bạn không có quyền nhận ticket này."));

        if (actorCandidate.getStatus() != TicketCandidateStatus.PENDING) {
            throw new TicketAlreadyClaimedException("Ticket đã được kỹ thuật viên khác nhận.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<TicketCandidateAssignment> assignments = candidateAssignmentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(requestId);

        request.setAssignedTo(actor);
        request.setStatus(MaintenanceStatus.IN_PROGRESS);
        request.setAssignmentSource(actorCandidate.getAssignmentSource());
        request.setLastActivityAt(now);

        for (TicketCandidateAssignment assignment : assignments) {
            if (actor.getId().equals(assignment.getTechnician().getId())) {
                assignment.setStatus(TicketCandidateStatus.ACCEPTED);
                assignment.setAcceptedAt(now);
                assignment.setRevokedAt(null);
                continue;
            }
            if (assignment.getStatus() == TicketCandidateStatus.PENDING) {
                assignment.setStatus(TicketCandidateStatus.REVOKED);
                assignment.setRevokedAt(now);
            }
        }

        candidateAssignmentRepository.saveAll(assignments);
        MaintenanceRequest saved = maintenanceRepository.save(request);

        Collection<User> visibleRecipients = visibleRecipients(saved);
        Collection<User> refreshRecipients = refreshRecipients(saved, assignments);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_CLAIMED",
                actor.getFullName() + " đã nhận ticket " + saved.getTicketCode() + ".",
                visibleRecipients,
                refreshRecipients
        );
        return saved;
    }

    public MaintenanceRequest resolve(Long requestId, String resolutionNote, User resolvedBy) {
        MaintenanceRequest request = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        if (!canResolveTicket(request, resolvedBy)) {
            throw new IllegalStateException("Chỉ quản trị viên hoặc kỹ thuật viên được giao mới có thể hoàn tất ticket đang xử lý.");
        }

        request.setStatus(MaintenanceStatus.RESOLVED);
        request.setResolutionNote(resolutionNote);
        request.setResolvedAt(LocalDateTime.now());
        request.setAssignedTo(resolvedBy);
        request.setLastActivityAt(LocalDateTime.now());

        request.getAsset().setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(request.getAsset());

        MaintenanceRequest saved = maintenanceRepository.save(request);
        Collection<User> recipients = visibleRecipients(saved);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_RESOLVED",
                resolvedBy.getFullName() + " đã giải quyết ticket " + saved.getTicketCode() + ".",
                recipients,
                recipients
        );
        return saved;
    }

    public MaintenanceRequest assign(Long requestId, User assignee) {
        MaintenanceRequest request = maintenanceRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        if (!OPEN_STATUSES.contains(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể phân công ticket còn mở.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<TicketCandidateAssignment> assignments = candidateAssignmentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(requestId);
        Map<Long, TicketCandidateAssignment> indexedAssignments = new LinkedHashMap<>();
        for (TicketCandidateAssignment assignment : assignments) {
            indexedAssignments.put(assignment.getTechnician().getId(), assignment);
        }

        TicketCandidateAssignment assigneeAssignment = indexedAssignments.get(assignee.getId());
        if (assigneeAssignment == null) {
            assigneeAssignment = TicketCandidateAssignment.builder()
                    .ticket(request)
                    .technician(assignee)
                    .status(TicketCandidateStatus.ACCEPTED)
                    .assignmentSource("MANUAL_OVERRIDE")
                    .acceptedAt(now)
                    .build();
            assignments.add(assigneeAssignment);
        } else {
            assigneeAssignment.setStatus(TicketCandidateStatus.ACCEPTED);
            assigneeAssignment.setAssignmentSource("MANUAL_OVERRIDE");
            assigneeAssignment.setAcceptedAt(now);
            assigneeAssignment.setRevokedAt(null);
        }

        for (TicketCandidateAssignment assignment : assignments) {
            if (assignee.getId().equals(assignment.getTechnician().getId())) {
                continue;
            }
            if (assignment.getStatus() != TicketCandidateStatus.REVOKED) {
                assignment.setStatus(TicketCandidateStatus.REVOKED);
                assignment.setRevokedAt(now);
            }
        }

        request.setAssignedTo(assignee);
        request.setAssignmentSource("MANUAL_OVERRIDE");
        request.setLastActivityAt(now);

        candidateAssignmentRepository.saveAll(assignments);
        MaintenanceRequest saved = maintenanceRepository.save(request);
        Collection<User> visibleRecipients = visibleRecipients(saved);
        Collection<User> refreshRecipients = refreshRecipients(saved, assignments);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_ASSIGNED",
                "Ticket " + saved.getTicketCode() + " đã được giao cho " + assignee.getFullName() + ".",
                visibleRecipients,
                refreshRecipients
        );
        return saved;
    }

    public MaintenanceRequest updateStatus(Long requestId, MaintenanceStatus status, User actor) {
        MaintenanceRequest request = maintenanceRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
        validateStatusTransition(request, status, actor);
        request.setStatus(status);
        request.setLastActivityAt(LocalDateTime.now());
        if (status == MaintenanceStatus.RESOLVED && request.getResolvedAt() == null) {
            request.setResolvedAt(LocalDateTime.now());
            request.getAsset().setStatus(AssetStatus.AVAILABLE);
            assetRepository.save(request.getAsset());
        }
        MaintenanceRequest saved = maintenanceRepository.save(request);
        Collection<User> recipients = visibleRecipients(saved);
        ticketRealtimeService.broadcastTicketChanged(
                saved,
                "TICKET_STATUS_CHANGED",
                "Ticket " + saved.getTicketCode() + " đã chuyển sang " + status.getDisplayName() + ".",
                recipients,
                recipients
        );
        return saved;
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
    public Page<MaintenanceRequest> searchTickets(String keyword,
                                                  MaintenanceStatus status,
                                                  Long viewerId,
                                                  Long reporterId,
                                                  Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? "%" + keyword.trim() + "%" : null;
        if (kw == null) {
            return maintenanceRepository.searchTicketApi(status, viewerId, reporterId, TicketCandidateStatus.PENDING, pageable);
        }
        return maintenanceRepository.searchTicketApiByKeyword(
                kw,
                status,
                viewerId,
                reporterId,
                TicketCandidateStatus.PENDING,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRequest> getOverdueTickets(Long viewerId, Long reporterId) {
        return maintenanceRepository.findOverdueTicketsApi(OPEN_STATUSES, viewerId, reporterId, TicketCandidateStatus.PENDING);
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
        if (currentOwner(ticket, user)) {
            return true;
        }
        return candidateAssignmentRepository.existsByTicketIdAndTechnicianIdAndStatus(
                ticket.getId(),
                user.getId(),
                TicketCandidateStatus.PENDING
        );
    }

    @Transactional(readOnly = true)
    public boolean canClaimTicket(MaintenanceRequest ticket, User user) {
        return ticket != null
                && user != null
                && MaintenanceStatus.PENDING.equals(ticket.getStatus())
                && ticket.getAssignedTo() == null
                && candidateAssignmentRepository.existsByTicketIdAndTechnicianIdAndStatus(
                        ticket.getId(),
                        user.getId(),
                        TicketCandidateStatus.PENDING
                );
    }

    public boolean canResolveTicket(MaintenanceRequest ticket, User user) {
        if (ticket == null || user == null || !MaintenanceStatus.IN_PROGRESS.equals(ticket.getStatus())) {
            return false;
        }
        return user.isAdmin() || currentOwner(ticket, user);
    }

    public boolean canCancelTicket(MaintenanceRequest ticket, User user) {
        return ticket != null
                && user != null
                && user.isAdmin()
                && OPEN_STATUSES.contains(ticket.getStatus());
    }

    public MaintenanceRequest save(MaintenanceRequest request) {
        return maintenanceRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<User> findPendingCandidateUsers(Long ticketId) {
        return candidateAssignmentRepository.findTechniciansByTicketIdAndStatus(ticketId, TicketCandidateStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<TicketCandidateAssignment> findCandidateAssignments(Long ticketId) {
        return candidateAssignmentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId);
    }

    public List<MaintenanceRequest> markOverdueTickets() {
        List<MaintenanceRequest> overdue = maintenanceRepository.findByStatusIn(OPEN_STATUSES);
        for (MaintenanceRequest ticket : overdue) {
            if (ticket.getSlaDueAt() != null
                    && LocalDateTime.now().isAfter(ticket.getSlaDueAt())
                    && ticket.getSlaBreachedAt() == null) {
                ticket.setSlaBreachedAt(LocalDateTime.now());
                maintenanceRepository.save(ticket);
                Collection<User> recipients = visibleRecipients(ticket);
                ticketRealtimeService.broadcastTicketChanged(
                        ticket,
                        "TICKET_OVERDUE",
                        "Ticket " + ticket.getTicketCode() + " đã quá hạn SLA.",
                        recipients,
                        recipients
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

    private List<TicketCandidateAssignment> createCandidateAssignments(MaintenanceRequest ticket, Asset asset, String issueType) {
        List<User> technicians = matchingTechnicians(asset, issueType);
        if (technicians.isEmpty()) {
            return List.of();
        }

        String assignmentSource = hasMatchingRules(asset, issueType) ? "AUTO_RULE" : "FALLBACK_ALL";
        List<TicketCandidateAssignment> assignments = technicians.stream()
                .map(technician -> TicketCandidateAssignment.builder()
                        .ticket(ticket)
                        .technician(technician)
                        .status(TicketCandidateStatus.PENDING)
                        .assignmentSource(assignmentSource)
                        .build())
                .toList();
        return candidateAssignmentRepository.saveAll(assignments);
    }

    private boolean hasMatchingRules(Asset asset, String issueType) {
        return coverageRuleRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc().stream()
                .filter(rule -> rule.getTechnician() != null && Boolean.TRUE.equals(rule.getTechnician().getIsActive()))
                .anyMatch(rule -> matches(rule, asset, issueType));
    }

    private List<User> matchingTechnicians(Asset asset, String issueType) {
        List<TechnicianCoverageRule> rules = coverageRuleRepository.findByIsActiveTrueOrderBySortOrderAscIdAsc();
        LinkedHashMap<Long, User> matched = rules.stream()
                .filter(rule -> rule.getTechnician() != null && Boolean.TRUE.equals(rule.getTechnician().getIsActive()))
                .filter(rule -> matches(rule, asset, issueType))
                .sorted(Comparator
                        .comparingInt((TechnicianCoverageRule rule) -> matchRank(rule, asset, issueType))
                        .thenComparing(TechnicianCoverageRule::getSortOrder)
                        .thenComparing(TechnicianCoverageRule::getId))
                .collect(
                        LinkedHashMap::new,
                        (map, rule) -> map.putIfAbsent(rule.getTechnician().getId(), rule.getTechnician()),
                        LinkedHashMap::putAll
                );

        if (!matched.isEmpty()) {
            return new ArrayList<>(matched.values());
        }

        return userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("MAINTENANCE");
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
            if (actor == null
                    || !MaintenanceStatus.PENDING.equals(ticket.getStatus())
                    || !currentOwner(ticket, actor)) {
                throw new IllegalStateException("Chỉ kỹ thuật viên đang sở hữu ticket mới có thể bắt đầu xử lý.");
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

    private boolean currentOwner(MaintenanceRequest ticket, User user) {
        return ticket.getAssignedTo() != null
                && user.getId() != null
                && user.getId().equals(ticket.getAssignedTo().getId());
    }

    public Collection<User> visibleRecipients(MaintenanceRequest ticket) {
        List<User> users = new ArrayList<>();
        users.add(ticket.getReportedBy());
        users.add(ticket.getAssignedTo());
        users.addAll(findPendingCandidateUsers(ticket.getId()));
        users.addAll(userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("ADMIN"));
        return distinctUsers(users);
    }

    public Collection<User> refreshRecipients(MaintenanceRequest ticket, Collection<TicketCandidateAssignment> assignments) {
        List<User> users = new ArrayList<>();
        users.add(ticket.getReportedBy());
        users.add(ticket.getAssignedTo());
        if (assignments != null) {
            assignments.stream().map(TicketCandidateAssignment::getTechnician).forEach(users::add);
        }
        users.addAll(userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("ADMIN"));
        return distinctUsers(users);
    }

    private Collection<User> distinctUsers(Collection<User> users) {
        LinkedHashMap<Long, User> indexed = new LinkedHashMap<>();
        for (User user : users) {
            if (user != null && user.getId() != null) {
                indexed.putIfAbsent(user.getId(), user);
            }
        }
        return new LinkedHashSet<>(indexed.values());
    }
}
