package com.dtoan.project.fptassetmanagement.config;

import com.dtoan.project.fptassetmanagement.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    @ModelAttribute("appNotifications")
    public Object appNotifications(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return java.util.List.of();
        }
        return notificationService.getNotificationsForUsername(authentication.getName());
    }

    @ModelAttribute("notificationBadgeCount")
    public long notificationBadgeCount(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return 0;
        }
        return notificationService.getUnreadNotificationCount(authentication.getName());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
