package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.AssetUsage;
import com.dtoan.project.fptassetmanagement.entity.AuditLog;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    public static final String TARGET_ASSET = "ASSET";
    public static final String TARGET_USER = "USER";

    private final AuditLogRepository auditLogRepository;

    public void log(User actor, String actionType, String targetType, Long targetId, String summary) {
        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .summary(summary)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAssetAuditLogs(Long assetId, Pageable pageable) {
        return auditLogRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(TARGET_ASSET, assetId, pageable);
    }

    public void logAssetCreated(Asset asset, User actor) {
        log(actor, "ASSET_CREATED", TARGET_ASSET, asset.getId(),
                actor.getFullName() + " đã tạo thiết bị " + asset.getName() + " (" + asset.getQaCode() + ").");
    }

    public void logAssetUpdated(Asset asset, User actor) {
        log(actor, "ASSET_UPDATED", TARGET_ASSET, asset.getId(),
                actor.getFullName() + " đã cập nhật thiết bị " + asset.getName() + " (" + asset.getQaCode() + ").");
    }

    public void logAssetDeleted(Asset asset, User actor) {
        log(actor, "ASSET_DELETED", TARGET_ASSET, asset.getId(),
                actor.getFullName() + " đã xóa thiết bị " + asset.getName() + " (" + asset.getQaCode() + ").");
    }

    public void logCheckIn(AssetUsage usage, User actor) {
        String destination = usage.getRoomTo() != null ? usage.getRoomTo().getName() : "giữ nguyên vị trí";
        log(actor, "ASSET_CHECKIN", TARGET_ASSET, usage.getAsset().getId(),
                actor.getFullName() + " đã check-in " + usage.getAsset().getName() + " tới " + destination + ".");
    }

    public void logCheckOut(AssetUsage usage, User actor) {
        log(actor, "ASSET_CHECKOUT", TARGET_ASSET, usage.getAsset().getId(),
                actor.getFullName() + " đã check-out " + usage.getAsset().getName() + " về kho.");
    }

    public void logMaintenanceAssigned(MaintenanceRequest request, User actor) {
        String assignee = request.getAssignedTo() != null ? request.getAssignedTo().getFullName() : "chưa phân công";
        log(actor, "MAINTENANCE_ASSIGNED", TARGET_ASSET, request.getAsset().getId(),
                actor.getFullName() + " đã phân công bảo trì cho " + assignee + ".");
    }

    public void logMaintenanceResolved(MaintenanceRequest request, User actor) {
        log(actor, "MAINTENANCE_RESOLVED", TARGET_ASSET, request.getAsset().getId(),
                actor.getFullName() + " đã xác nhận sửa xong cho " + request.getAsset().getName() + ".");
    }

    public void logAssetMarkedLost(Asset asset, User actor) {
        logAssetMarkedLost(asset, actor, null);
    }

    public void logAssetMarkedLost(Asset asset, User actor, String note) {
        String lastKnownRoom = asset.getRoom() != null ? asset.getRoom().getName() : "chưa xác định";
        String noteSuffix = note != null && !note.isBlank() ? " Ghi chú: " + note.trim() : "";
        log(actor, "ASSET_MARKED_LOST", TARGET_ASSET, asset.getId(),
                actor.getFullName() + " đã đánh dấu thất lạc thiết bị " + asset.getName()
                        + ". Vị trí ghi nhận cuối: " + lastKnownRoom + "." + noteSuffix);
    }

    public void logAssetMarkedFound(Asset asset, User actor) {
        String currentRoom = asset.getRoom() != null ? asset.getRoom().getName() : "chưa xác định";
        log(actor, "ASSET_MARKED_FOUND", TARGET_ASSET, asset.getId(),
                actor.getFullName() + " đã đánh dấu tìm thấy thiết bị " + asset.getName()
                        + " và chuyển về " + currentRoom + ".");
    }

    public void logUserRoleChanged(User changedUser, String oldRoleName, String newRoleName, User actor) {
        log(actor, "USER_ROLE_CHANGED", TARGET_USER, changedUser.getId(),
                actor.getFullName() + " đã đổi quyền của " + changedUser.getFullName() +
                        " từ " + oldRoleName + " sang " + newRoleName + ".");
    }
}
