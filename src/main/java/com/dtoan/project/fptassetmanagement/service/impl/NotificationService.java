package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.dto.AppNotification;
import com.dtoan.project.fptassetmanagement.entity.AssetUsage;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.Notification;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetUsageRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.repository.NotificationRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int OVERDUE_MAINTENANCE_DAYS = 7;
    private static final int MAX_EVENT_NOTIFICATIONS = 12;
    private static final List<MaintenanceStatus> OPEN_MAINTENANCE_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);
    private static final List<String> SYNCED_NOTIFICATION_PREFIXES = List.of(
            "usage-checkin-",
            "usage-checkout-",
            "maintenance-created-",
            "maintenance-overdue-"
    );

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final AssetUsageRepository assetUsageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<AppNotification> getNotificationsForUsername(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }

        return notificationRepository.findTop10ByUserIdAndIsReadFalseAndIsArchivedFalseOrderByUpdatedAtDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(String username) {
        if (username == null || username.isBlank()) {
            return 0;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return 0;
        }

        return notificationRepository.countByUserIdAndIsReadFalseAndIsArchivedFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public List<AppNotification> getRecentNotificationsForUsername(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }

        return notificationRepository.findTop10ByUserIdAndIsArchivedFalseOrderByIsReadAscUpdatedAtDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo"));
        archiveNotification(notification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        List<Notification> notifications = notificationRepository.findByUserIdAndIsArchivedFalse(user.getId());
        notifications.forEach(this::archiveNotification);
    }

    @Transactional
    public void pushNotification(User user,
                                 String key,
                                 String title,
                                 String message,
                                 String href,
                                 String icon,
                                 String tone) {
        Notification notification = notificationRepository
                .findByUserIdAndNotificationKeyAndIsArchivedFalse(user.getId(), key)
                .orElseGet(() -> Notification.builder()
                        .user(user)
                        .notificationKey(key)
                        .count(0L)
                        .build());

        notification.setTitle(title);
        notification.setMessage(message);
        notification.setHref(href);
        notification.setIcon(icon);
        notification.setTone(tone);
        notification.setCount(notification.getCount() == null ? 1L : notification.getCount() + 1L);
        notification.setIsRead(false);
        notification.setIsArchived(false);

        Notification saved = notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                "/queue/notifications",
                toDto(saved)
        );
    }

    @Transactional
    public void pushNotification(Collection<User> users,
                                 String keyPrefix,
                                 String title,
                                 String message,
                                 String href,
                                 String icon,
                                 String tone) {
        Set<Long> seenUserIds = new HashSet<>();
        for (User user : users) {
            if (user == null || user.getId() == null || !seenUserIds.add(user.getId())) {
                continue;
            }
            pushNotification(
                    user,
                    keyPrefix + "-" + user.getId(),
                    title,
                    message,
                    href,
                    icon,
                    tone
            );
        }
    }

    @Transactional
    public void pushCheckInNotification(AssetUsage usage, User actor) {
        pushNotification(
                activeAdminsExcept(actor),
                "usage-checkin-" + usage.getId(),
                "Check-in mới",
                buildCheckInMessage(usage),
                "/usages",
                "bi-box-arrow-in-right",
                "primary"
        );
    }

    @Transactional
    public void pushCheckOutNotification(AssetUsage usage, User actor) {
        pushNotification(
                activeAdminsExcept(actor),
                "usage-checkout-" + usage.getId(),
                "Check-out mới",
                buildCheckOutMessage(usage),
                "/usages",
                "bi-box-arrow-left",
                "success"
        );
    }

    private List<AppNotification> buildSystemNotifications() {
        List<AppNotification> notifications = new ArrayList<>();
        LocalDateTime overdueCutoff = LocalDateTime.now().minusDays(OVERDUE_MAINTENANCE_DAYS);

        for (AssetUsage usage : assetUsageRepository.findTop10ByOrderByCheckInTimeDesc()) {
            notifications.add(AppNotification.builder()
                    .key("usage-checkin-" + usage.getId())
                    .title("Check-in mới")
                    .message(buildCheckInMessage(usage))
                    .icon("bi-box-arrow-in-right")
                    .tone("primary")
                    .href("/usages")
                    .count(1)
                    .createdAt(usage.getCheckInTime())
                    .updatedAt(usage.getCheckInTime())
                    .build());
        }

        for (AssetUsage usage : assetUsageRepository.findTop10ByCheckOutTimeIsNotNullOrderByCheckOutTimeDesc()) {
            notifications.add(AppNotification.builder()
                    .key("usage-checkout-" + usage.getId())
                    .title("Check-out mới")
                    .message(buildCheckOutMessage(usage))
                    .icon("bi-box-arrow-left")
                    .tone("success")
                    .href("/usages")
                    .count(1)
                    .createdAt(usage.getCheckOutTime())
                    .updatedAt(usage.getCheckOutTime())
                    .build());
        }

        for (MaintenanceRequest request : maintenanceRequestRepository.findTop10ByOrderByReportedAtDesc()) {
            notifications.add(AppNotification.builder()
                    .key("maintenance-created-" + request.getId())
                    .title("Yêu cầu bảo trì mới")
                    .message(buildMaintenanceCreatedMessage(request))
                    .icon("bi-tools")
                    .tone("warning")
                    .href("/tickets")
                    .count(1)
                    .createdAt(request.getReportedAt())
                    .updatedAt(request.getReportedAt())
                    .build());
        }

        for (MaintenanceRequest request : maintenanceRequestRepository
                .findTop10ByStatusInAndReportedAtBeforeOrderByReportedAtDesc(OPEN_MAINTENANCE_STATUSES, overdueCutoff)) {
            notifications.add(AppNotification.builder()
                    .key("maintenance-overdue-" + request.getId())
                    .title("Bảo trì quá hạn")
                    .message(buildMaintenanceOverdueMessage(request))
                    .icon("bi-hourglass-split")
                    .tone("danger")
                    .href("/tickets")
                    .count(1)
                    .createdAt(request.getReportedAt())
                    .updatedAt(request.getReportedAt())
                    .build());
        }

        return notifications.stream()
                .sorted(Comparator.comparing(AppNotification::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(MAX_EVENT_NOTIFICATIONS)
                .toList();
    }

    private void syncNotifications(User user) {
        List<AppNotification> systemNotifications = buildSystemNotifications();
        Map<String, Notification> existingByKey = new HashMap<>();
        for (Notification notification : notificationRepository.findByUserIdAndIsArchivedFalse(user.getId())) {
            if (isSyncedNotificationKey(notification.getNotificationKey())) {
                existingByKey.put(notification.getNotificationKey(), notification);
            }
        }

        Set<String> activeKeys = new HashSet<>();
        for (AppNotification systemNotification : systemNotifications) {
            activeKeys.add(systemNotification.getKey());

            Notification existing = existingByKey.get(systemNotification.getKey());
            if (existing == null) {
                if (notificationRepository.findByUserIdAndNotificationKeyAndIsArchivedTrue(user.getId(),
                        systemNotification.getKey()).isPresent()) {
                    continue;
                }
                notificationRepository.save(Notification.builder()
                        .user(user)
                        .notificationKey(systemNotification.getKey())
                        .title(systemNotification.getTitle())
                        .message(systemNotification.getMessage())
                        .icon(systemNotification.getIcon())
                        .tone(systemNotification.getTone())
                        .href(systemNotification.getHref())
                        .count(systemNotification.getCount())
                        .isRead(false)
                        .isArchived(false)
                        .build());
                continue;
            }

            boolean changed = existing.getCount() != systemNotification.getCount()
                    || !existing.getMessage().equals(systemNotification.getMessage())
                    || !existing.getTitle().equals(systemNotification.getTitle());

            existing.setTitle(systemNotification.getTitle());
            existing.setMessage(systemNotification.getMessage());
            existing.setIcon(systemNotification.getIcon());
            existing.setTone(systemNotification.getTone());
            existing.setHref(systemNotification.getHref());
            existing.setCount(systemNotification.getCount());
            existing.setIsArchived(false);
            if (changed) {
                existing.setIsRead(false);
            }
            notificationRepository.save(existing);
        }

        for (Notification notification : existingByKey.values()) {
            if (!activeKeys.contains(notification.getNotificationKey())) {
                archiveNotification(notification);
            }
        }
    }

    private boolean isSyncedNotificationKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        return SYNCED_NOTIFICATION_PREFIXES.stream().anyMatch(key::startsWith);
    }

    private void archiveNotification(Notification notification) {
        Notification archivedNotification = notificationRepository
                .findByUserIdAndNotificationKeyAndIsArchivedTrue(notification.getUser().getId(),
                        notification.getNotificationKey())
                .orElse(null);

        if (archivedNotification != null && !archivedNotification.getId().equals(notification.getId())) {
            archivedNotification.setTitle(notification.getTitle());
            archivedNotification.setMessage(notification.getMessage());
            archivedNotification.setIcon(notification.getIcon());
            archivedNotification.setTone(notification.getTone());
            archivedNotification.setHref(notification.getHref());
            archivedNotification.setCount(notification.getCount());
            archivedNotification.setIsRead(true);
            archivedNotification.setIsArchived(true);
            notificationRepository.save(archivedNotification);
            notificationRepository.delete(notification);
            return;
        }

        notification.setIsRead(true);
        notification.setIsArchived(true);
        notificationRepository.save(notification);
    }

    private AppNotification toDto(Notification notification) {
        return AppNotification.builder()
                .id(notification.getId())
                .key(notification.getNotificationKey())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .icon(notification.getIcon())
                .tone(notification.getTone())
                .href(notification.getHref())
                .count(notification.getCount())
                .read(Boolean.TRUE.equals(notification.getIsRead()))
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    private List<User> activeAdminsExcept(User actor) {
        Long actorId = actor != null ? actor.getId() : null;
        return userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("ADMIN")
                .stream()
                .filter(user -> actorId == null || !actorId.equals(user.getId()))
                .toList();
    }

    private String buildCheckInMessage(AssetUsage usage) {
        String room = usage.getRoomTo() != null ? usage.getRoomTo().getName() : "không đổi phòng";
        return usage.getUser().getFullName() + " vừa check-in thiết bị " +
                usage.getAsset().getName() + " (" + usage.getAsset().getQaCode() + ") tới " + room + ".";
    }

    private String buildCheckOutMessage(AssetUsage usage) {
        return usage.getUser().getFullName() + " vừa check-out thiết bị " +
                usage.getAsset().getName() + " (" + usage.getAsset().getQaCode() + ").";
    }

    private String buildMaintenanceCreatedMessage(MaintenanceRequest request) {
        return request.getReportedBy().getFullName() + " vừa tạo yêu cầu " +
                request.getIssueTypeDisplayName().toLowerCase() + " cho thiết bị " +
                request.getAsset().getName() + " (" + request.getAsset().getQaCode() + ").";
    }

    private String buildMaintenanceOverdueMessage(MaintenanceRequest request) {
        long days = java.time.Duration.between(request.getReportedAt(), LocalDateTime.now()).toDays();
        return "Yêu cầu cho thiết bị " + request.getAsset().getName() + " (" + request.getAsset().getQaCode() +
                ") đã chờ xử lý " + days + " ngày.";
    }
}
