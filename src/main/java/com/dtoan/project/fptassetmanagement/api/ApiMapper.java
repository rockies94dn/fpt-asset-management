package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.dto.AppNotification;
import com.dtoan.project.fptassetmanagement.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiMapper {

    public ApiDtos.UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new ApiDtos.UserDto(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? user.getRole().getName() : null,
                Boolean.TRUE.equals(user.getIsActive())
        );
    }

    public ApiDtos.RoomDto toRoomDto(Room room) {
        if (room == null) {
            return null;
        }
        return new ApiDtos.RoomDto(
                room.getId(),
                room.getCode(),
                room.getName(),
                room.getBuilding(),
                room.getFloor(),
                room.getCapacity(),
                Boolean.TRUE.equals(room.getIsActive())
        );
    }

    public ApiDtos.CategoryDto toCategoryDto(AssetCategory category) {
        if (category == null) {
            return null;
        }
        return new ApiDtos.CategoryDto(category.getId(), category.getName(), category.getIcon());
    }

    public ApiDtos.AssetDto toAssetDto(Asset asset) {
        if (asset == null) {
            return null;
        }
        return new ApiDtos.AssetDto(
                asset.getId(),
                asset.getQaCode(),
                asset.getName(),
                asset.getStatus() != null ? asset.getStatus().name() : null,
                asset.getStatus() != null ? asset.getStatus().getDisplayName() : null,
                toCategoryDto(asset.getCategory()),
                toRoomDto(asset.getRoom()),
                asset.getBrand(),
                asset.getModel(),
                asset.getSerialNumber(),
                asset.getPurchaseDate(),
                asset.getPurchasePrice(),
                asset.getWarrantyExpiry(),
                asset.getDescription(),
                asset.getCreatedAt(),
                asset.getUpdatedAt(),
                asset.isAvailable(),
                asset.canBeUsed()
        );
    }

    public ApiDtos.AssetMaintenanceHistoryDto toAssetMaintenanceHistoryDto(MaintenanceRequest ticket) {
        if (ticket == null) {
            return null;
        }
        return new ApiDtos.AssetMaintenanceHistoryDto(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getIssueType(),
                ticket.getIssueTypeDisplayName(),
                ticket.getPriority(),
                ticket.getPriorityDisplayName(),
                ticket.getStatus() != null ? ticket.getStatus().name() : null,
                ticket.getStatus() != null ? ticket.getStatus().getDisplayName() : null,
                ticket.getReportedRoomSnapshot(),
                ticket.getResolutionNote(),
                ticket.getReportedAt(),
                ticket.getResolvedAt(),
                ticket.getLastActivityAt(),
                ticket.isOverdue(),
                toUserDto(ticket.getReportedBy()),
                toUserDto(ticket.getAssignedTo())
        );
    }

    public ApiDtos.AssetDetailDto toAssetDetailDto(Asset asset, List<MaintenanceRequest> history) {
        return new ApiDtos.AssetDetailDto(
                toAssetDto(asset),
                history.stream().map(this::toAssetMaintenanceHistoryDto).toList()
        );
    }

    public ApiDtos.TicketDto toTicketDto(MaintenanceRequest ticket) {
        if (ticket == null) {
            return null;
        }
        return new ApiDtos.TicketDto(
                ticket.getId(),
                ticket.getTicketCode(),
                toAssetDto(ticket.getAsset()),
                toUserDto(ticket.getReportedBy()),
                toUserDto(ticket.getAssignedTo()),
                ticket.getIssueType(),
                ticket.getIssueTypeDisplayName(),
                ticket.getPriority(),
                ticket.getPriorityDisplayName(),
                ticket.getStatus() != null ? ticket.getStatus().name() : null,
                ticket.getStatus() != null ? ticket.getStatus().getDisplayName() : null,
                ticket.getReportedRoomSnapshot(),
                ticket.getResolutionNote(),
                ticket.getAssignmentSource(),
                ticket.getReportedAt(),
                ticket.getResolvedAt(),
                ticket.getSlaDueAt(),
                ticket.getSlaBreachedAt(),
                ticket.getLastActivityAt(),
                ticket.isOverdue()
        );
    }

    public ApiDtos.ChatMessageDto toChatMessageDto(ChatMessage message) {
        return new ApiDtos.ChatMessageDto(
                message.getId(),
                message.getMessage(),
                message.getMessageType(),
                Boolean.TRUE.equals(message.getIsSystem()),
                message.getCreatedAt(),
                toUserDto(message.getSender())
        );
    }

    public ApiDtos.TicketAttachmentDto toAttachmentDto(TicketAttachment attachment) {
        return new ApiDtos.TicketAttachmentDto(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getSize(),
                "/api/tickets/" + attachment.getTicket().getId() + "/attachments/" + attachment.getId(),
                attachment.getCreatedAt(),
                toUserDto(attachment.getUploadedBy())
        );
    }

    public ApiDtos.UsageDto toUsageDto(AssetUsage usage) {
        return new ApiDtos.UsageDto(
                usage.getId(),
                toAssetDto(usage.getAsset()),
                toUserDto(usage.getUser()),
                toRoomDto(usage.getRoomFrom()),
                toRoomDto(usage.getRoomTo()),
                usage.getCheckInTime(),
                usage.getCheckOutTime(),
                usage.getPurpose(),
                usage.getNote(),
                usage.getStatus() != null ? usage.getStatus().name() : null
        );
    }

    public ApiDtos.NotificationDto toNotificationDto(AppNotification notification) {
        return new ApiDtos.NotificationDto(
                notification.getId(),
                notification.getKey(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getIcon(),
                notification.getTone(),
                notification.getHref(),
                notification.getCount(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    public ApiDtos.CoverageRuleDto toCoverageRuleDto(TechnicianCoverageRule rule) {
        return new ApiDtos.CoverageRuleDto(
                rule.getId(),
                toUserDto(rule.getTechnician()),
                toRoomDto(rule.getRoom()),
                toCategoryDto(rule.getCategory()),
                rule.getIssueType(),
                rule.getSortOrder(),
                Boolean.TRUE.equals(rule.getIsActive())
        );
    }

    public <T, R> ApiDtos.PageDto<R> toPageDto(Page<T> page, java.util.function.Function<T, R> mapper) {
        return new ApiDtos.PageDto<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public ApiDtos.TicketDetailDto toTicketDetailDto(MaintenanceRequest ticket,
                                                     List<ChatMessage> messages,
                                                     List<TicketAttachment> attachments,
                                                     boolean claimable,
                                                     List<User> candidateTechnicians) {
        return new ApiDtos.TicketDetailDto(
                toTicketDto(ticket),
                messages.stream().map(this::toChatMessageDto).toList(),
                attachments.stream().map(this::toAttachmentDto).toList(),
                claimable,
                candidateTechnicians.stream().map(this::toUserDto).toList()
        );
    }
}
