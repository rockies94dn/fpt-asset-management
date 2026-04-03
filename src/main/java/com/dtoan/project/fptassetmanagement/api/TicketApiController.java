package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.ChatMessage;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.TicketAttachment;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import com.dtoan.project.fptassetmanagement.service.impl.TicketAttachmentStorageService;
import com.dtoan.project.fptassetmanagement.service.impl.TicketChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketApiController {

    private final MaintenanceService maintenanceService;
    private final AssetService assetService;
    private final TicketChatService ticketChatService;
    private final TicketAttachmentStorageService attachmentStorageService;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    @GetMapping
    public ApiDtos.PageDto<ApiDtos.TicketDto> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) MaintenanceStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        Long viewerId = null;
        Long reporterId = null;
        if (!user.isAdmin()) {
            if (currentUserService.hasRole(user, "MAINTENANCE")) {
                viewerId = user.getId();
            } else {
                reporterId = user.getId();
            }
        }
        return apiMapper.toPageDto(
                maintenanceService.searchTickets(keyword, status, viewerId, reporterId, PageRequest.of(page, size)),
                apiMapper::toTicketDto
        );
    }

    @GetMapping("/overdue")
    public List<ApiDtos.TicketDto> overdueList(Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        Long viewerId = null;
        Long reporterId = null;
        if (!user.isAdmin()) {
            if (currentUserService.hasRole(user, "MAINTENANCE")) {
                viewerId = user.getId();
            } else {
                reporterId = user.getId();
            }
        }
        return maintenanceService.getOverdueTickets(viewerId, reporterId).stream()
                .map(apiMapper::toTicketDto)
                .toList();
    }

    @GetMapping("/meta")
    public Map<String, Object> meta() {
        return Map.of(
                "statuses", Arrays.stream(MaintenanceStatus.values()).map(status -> Map.of(
                        "value", status.name(),
                        "label", status.getDisplayName()
                )).toList(),
                "priorities", List.of(
                        Map.of("value", "LOW", "label", "Thấp"),
                        Map.of("value", "NORMAL", "label", "Trung bình"),
                        Map.of("value", "HIGH", "label", "Cao")
                ),
                "issueTypes", List.of(
                        Map.of("value", "BROKEN", "label", "Báo hỏng"),
                        Map.of("value", "MAINTENANCE", "label", "Bảo trì"),
                        Map.of("value", "UPGRADE", "label", "Nâng cấp")
                ),
                "technicians", userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("MAINTENANCE").stream()
                        .map(apiMapper::toUserDto).toList()
        );
    }

    @GetMapping("/{id}")
    public ApiDtos.TicketDetailDto detail(@PathVariable Long id, Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        MaintenanceRequest ticket = requireAccessibleTicket(id, authentication);
        return toTicketDetailDto(ticket, user);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiDtos.TicketDetailDto create(@RequestParam String qaCode,
                                          @RequestParam String issueType,
                                          @RequestParam String description,
                                          @RequestParam(defaultValue = "NORMAL") String priority,
                                          @RequestPart(required = false) MultipartFile attachment,
                                          Authentication authentication) throws IOException {
        User user = currentUserService.requireUser(authentication);
        Asset asset = assetService.findByQaCode(qaCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
        MaintenanceRequest ticket = maintenanceService.createRequest(asset, user, issueType, description, priority);
        if (attachment != null && !attachment.isEmpty()) {
            attachmentStorageService.store(ticket, user, attachment);
        }
        ticketChatService.systemMessage(ticket, user, user.getFullName() + " đã tạo ticket " + ticket.getTicketCode() + ".");
        return toTicketDetailDto(ticket, user);
    }

    @PostMapping("/{id}/assign")
    public ApiDtos.TicketDto assign(@PathVariable Long id,
                                    @RequestBody ApiDtos.AssignTicketRequest request,
                                    Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        if (!actor.isAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới được phân công ticket.");
        }
        User assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ thuật viên."));
        MaintenanceRequest ticket = maintenanceService.assign(id, assignee);
        ticketChatService.systemMessage(ticket, actor, "Ticket được giao cho " + assignee.getFullName() + ".");
        return apiMapper.toTicketDto(ticket);
    }

    @PostMapping("/{id}/claim")
    public ApiDtos.TicketDto claim(@PathVariable Long id, Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        MaintenanceRequest ticket = maintenanceService.claim(id, actor);
        ticketChatService.systemMessage(ticket, actor, actor.getFullName() + " đã nhận việc.");
        return apiMapper.toTicketDto(ticket);
    }

    @PostMapping("/{id}/status")
    public ApiDtos.TicketDto updateStatus(@PathVariable Long id,
                                          @RequestBody ApiDtos.UpdateTicketStatusRequest request,
                                          Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        MaintenanceRequest ticket = requireAccessibleTicket(id, authentication);
        if (!actor.isAdmin() && !currentUserService.hasRole(actor, "MAINTENANCE")) {
            throw new IllegalStateException("Bạn không có quyền cập nhật trạng thái ticket.");
        }
        MaintenanceRequest updated = maintenanceService.updateStatus(id, MaintenanceStatus.valueOf(request.status()), actor);
        ticketChatService.systemMessage(updated, actor, actor.getFullName() + " đã cập nhật trạng thái sang " + updated.getStatus().getDisplayName() + ".");
        return apiMapper.toTicketDto(updated);
    }

    @PostMapping("/{id}/resolve")
    public ApiDtos.TicketDto resolve(@PathVariable Long id,
                                     @RequestBody ApiDtos.ResolveTicketRequest request,
                                     Authentication authentication) {
        User actor = currentUserService.requireUser(authentication);
        MaintenanceRequest ticket = requireAccessibleTicket(id, authentication);
        if (!actor.isAdmin() && !currentUserService.hasRole(actor, "MAINTENANCE")) {
            throw new IllegalStateException("Bạn không có quyền giải quyết ticket.");
        }
        MaintenanceRequest resolved = maintenanceService.resolve(ticket.getId(), request.resolutionNote(), actor);
        ticketChatService.systemMessage(resolved, actor, actor.getFullName() + " đã đánh dấu ticket là đã giải quyết.");
        return apiMapper.toTicketDto(resolved);
    }

    @GetMapping("/{id}/messages")
    public List<ApiDtos.ChatMessageDto> messages(@PathVariable Long id, Authentication authentication) {
        requireAccessibleTicket(id, authentication);
        return ticketChatService.getMessages(id).stream().map(apiMapper::toChatMessageDto).toList();
    }

    @PostMapping("/{id}/messages")
    public ApiDtos.ChatMessageDto sendMessage(@PathVariable Long id,
                                              @RequestBody ApiDtos.SendChatMessageRequest request,
                                              Authentication authentication) {
        MaintenanceRequest ticket = requireAccessibleTicket(id, authentication);
        User user = currentUserService.requireUser(authentication);
        ChatMessage message = ticketChatService.sendMessage(ticket, user, request.message());
        return apiMapper.toChatMessageDto(message);
    }

    @PostMapping(path = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiDtos.TicketAttachmentDto uploadAttachment(@PathVariable Long id,
                                                        @RequestPart MultipartFile file,
                                                        Authentication authentication) throws IOException {
        MaintenanceRequest ticket = requireAccessibleTicket(id, authentication);
        User user = currentUserService.requireUser(authentication);
        TicketAttachment attachment = attachmentStorageService.store(ticket, user, file);
        return apiMapper.toAttachmentDto(attachment);
    }

    @GetMapping("/{ticketId}/attachments/{attachmentId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long ticketId,
                                                       @PathVariable Long attachmentId,
                                                       Authentication authentication) throws IOException {
        requireAccessibleTicket(ticketId, authentication);
        TicketAttachment attachment = attachmentStorageService.findById(ticketId, attachmentId);
        Resource resource = new FileSystemResource(attachmentStorageService.resolvePath(attachment));
        String contentType = Files.probeContentType(attachmentStorageService.resolvePath(attachment));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType == null ? attachment.getContentType() : contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getOriginalName() + "\"")
                .body(resource);
    }

    private MaintenanceRequest requireAccessibleTicket(Long id, Authentication authentication) {
        User user = currentUserService.requireUser(authentication);
        MaintenanceRequest ticket = maintenanceService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket."));
        if (!maintenanceService.canAccessTicket(ticket, user)) {
            throw new IllegalStateException("Bạn không có quyền truy cập ticket này.");
        }
        return ticket;
    }

    private ApiDtos.TicketDetailDto toTicketDetailDto(MaintenanceRequest ticket, User user) {
        return apiMapper.toTicketDetailDto(
                ticket,
                ticketChatService.getMessages(ticket.getId()),
                attachmentStorageService.listForTicket(ticket.getId()),
                maintenanceService.canClaimTicket(ticket, user),
                maintenanceService.findPendingCandidateUsers(ticket.getId())
        );
    }
}
