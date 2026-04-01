package com.dtoan.project.fptassetmanagement.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record AuthRequest(String username, String password) {
    }

    public record UserDto(
            Long id,
            String username,
            String fullName,
            String email,
            String phone,
            String role,
            boolean active
    ) {
    }

    public record RoomDto(
            Long id,
            String code,
            String name,
            String building,
            Integer floor,
            Integer capacity,
            boolean active
    ) {
    }

    public record CategoryDto(
            Long id,
            String name,
            String icon
    ) {
    }

    public record AssetDto(
            Long id,
            String qaCode,
            String name,
            String status,
            String statusLabel,
            CategoryDto category,
            RoomDto room,
            String brand,
            String model,
            String serialNumber,
            LocalDate purchaseDate,
            BigDecimal purchasePrice,
            LocalDate warrantyExpiry,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean available,
            boolean canBeUsed
    ) {
    }

    public record AssetMaintenanceHistoryDto(
            Long id,
            String ticketCode,
            String issueType,
            String issueTypeLabel,
            String priority,
            String priorityLabel,
            String status,
            String statusLabel,
            String reportedRoomSnapshot,
            String resolutionNote,
            LocalDateTime reportedAt,
            LocalDateTime resolvedAt,
            LocalDateTime lastActivityAt,
            boolean overdue,
            UserDto reportedBy,
            UserDto assignedTo
    ) {
    }

    public record AssetDetailDto(
            AssetDto asset,
            List<AssetMaintenanceHistoryDto> maintenanceHistory
    ) {
    }

    public record TicketAttachmentDto(
            Long id,
            String originalName,
            String contentType,
            Long size,
            String downloadUrl,
            LocalDateTime createdAt,
            UserDto uploadedBy
    ) {
    }

    public record ChatMessageDto(
            Long id,
            String message,
            String messageType,
            boolean system,
            LocalDateTime createdAt,
            UserDto sender
    ) {
    }

    public record TicketDto(
            Long id,
            String ticketCode,
            AssetDto asset,
            UserDto reportedBy,
            UserDto assignedTo,
            String issueType,
            String issueTypeLabel,
            String priority,
            String priorityLabel,
            String status,
            String statusLabel,
            String reportedRoomSnapshot,
            String resolutionNote,
            String assignmentSource,
            LocalDateTime reportedAt,
            LocalDateTime resolvedAt,
            LocalDateTime slaDueAt,
            LocalDateTime slaBreachedAt,
            LocalDateTime lastActivityAt,
            boolean overdue
    ) {
    }

    public record TicketDetailDto(
            TicketDto ticket,
            List<ChatMessageDto> messages,
            List<TicketAttachmentDto> attachments
    ) {
    }

    public record UsageDto(
            Long id,
            AssetDto asset,
            UserDto user,
            RoomDto roomFrom,
            RoomDto roomTo,
            LocalDateTime checkInTime,
            LocalDateTime checkOutTime,
            String purpose,
            String note,
            String status
    ) {
    }

    public record NotificationDto(
            Long id,
            String key,
            String title,
            String message,
            String icon,
            String tone,
            String href,
            long count,
            boolean read,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    public record CoverageRuleDto(
            Long id,
            UserDto technician,
            RoomDto room,
            CategoryDto category,
            String issueType,
            Integer sortOrder,
            boolean active
    ) {
    }

    public record DashboardSummaryDto(
            long totalAssets,
            long availableAssets,
            long inUseAssets,
            long brokenAssets,
            long maintenanceAssets,
            long lostAssets,
            long activeUsages,
            long pendingMaintenance,
            long openTickets,
            long overdueTickets
    ) {
    }

    public record DashboardResponse(
            DashboardSummaryDto summary,
            List<AssetDto> recentAssets,
            List<AssetDto> attentionAssets,
            List<TicketDto> myTickets
    ) {
    }

    public record PageDto<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
    }

    public record MeResponse(
            boolean authenticated,
            String csrfToken,
            UserDto user
    ) {
    }

    public record SimpleMessageResponse(String message) {
    }

    public record AssetUpsertRequest(
            String qaCode,
            String name,
            Long categoryId,
            Long roomId,
            String status,
            String brand,
            String model,
            String serialNumber,
            LocalDate purchaseDate,
            BigDecimal purchasePrice,
            LocalDate warrantyExpiry,
            String description,
            boolean autoGenerateCode
    ) {
    }

    public record TicketCreateRequest(
            String qaCode,
            String issueType,
            String description,
            String priority
    ) {
    }

    public record AssignTicketRequest(Long assigneeId) {
    }

    public record UpdateTicketStatusRequest(String status) {
    }

    public record ResolveTicketRequest(String resolutionNote) {
    }

    public record SendChatMessageRequest(String message) {
    }

    public record CheckInRequest(String qaCode, Long roomToId, String purpose) {
    }

    public record CheckOutRequest(String note) {
    }

    public record UserUpdateRequest(
            String fullName,
            String username,
            String email,
            String phone,
            Long roleId
    ) {
    }

    public record PasswordResetRequest(String newPassword) {
    }

    public record RoomUpsertRequest(
            String code,
            String name,
            String building,
            Integer floor,
            Integer capacity,
            String description
    ) {
    }

    public record CoverageRuleSaveRequest(
            Long technicianId,
            Long roomId,
            Long categoryId,
            String issueType,
            Integer sortOrder,
            Boolean active
    ) {
    }
}
