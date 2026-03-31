package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id,
                             @RequestParam(required = false) String redirectTo,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id, userDetails.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirect(redirectTo);
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@RequestParam(required = false) String redirectTo,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAllAsRead(userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Đã đọc và ẩn tất cả thông báo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirect(redirectTo);
    }

    private String redirect(String redirectTo) {
        if (redirectTo == null || redirectTo.isBlank() || !redirectTo.startsWith("/")) {
            return "redirect:/dashboard";
        }
        return "redirect:" + redirectTo;
    }
}
