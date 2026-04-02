package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.service.impl.NotificationService;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;

    @GetMapping
    public List<ApiDtos.NotificationDto> list(Authentication authentication) {
        String username = currentUserService.requireUser(authentication).getUsername();
        return notificationService.getRecentNotificationsForUsername(username)
                .stream()
                .map(apiMapper::toNotificationDto)
                .toList();
    }

    @GetMapping("/count")
    public Map<String, Long> count(Authentication authentication) {
        String username = currentUserService.requireUser(authentication).getUsername();
        return Map.of("unreadCount", notificationService.getUnreadNotificationCount(username));
    }

    @PostMapping("/{id}/read")
    public ApiDtos.SimpleMessageResponse markAsRead(@PathVariable Long id, Authentication authentication) {
        String username = currentUserService.requireUser(authentication).getUsername();
        notificationService.markAsRead(id, username);
        return new ApiDtos.SimpleMessageResponse("Đã đánh dấu thông báo là đã đọc.");
    }

    @PostMapping("/read-all")
    public ApiDtos.SimpleMessageResponse markAllAsRead(Authentication authentication) {
        String username = currentUserService.requireUser(authentication).getUsername();
        notificationService.markAllAsRead(username);
        return new ApiDtos.SimpleMessageResponse("Đã đọc tất cả thông báo.");
    }
}
