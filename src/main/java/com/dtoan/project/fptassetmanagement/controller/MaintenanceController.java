package com.dtoan.project.fptassetmanagement.controller;
import com.dtoan.project.fptassetmanagement.entity.*;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.AuditLogService;
import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AssetService assetService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) MaintenanceStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("requests", maintenanceService.searchRequests(keyword, status, pageable));
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("canResolveMaintenance", canResolveMaintenance(userDetails));
        model.addAttribute("canAssignMaintenance", hasRole(userDetails, "ADMIN"));
        model.addAttribute("maintenanceUsers", userRepository.findByRoleNameAndIsActiveTrueOrderByFullNameAsc("MAINTENANCE"));
        return "maintenance/list";
    }

    @GetMapping("/report")
    public String reportForm(@RequestParam(required = false) String qaCode, Model model) {
        model.addAttribute("qaCode", qaCode);
        if (qaCode != null) assetService.findByQaCode(qaCode).ifPresent(a -> model.addAttribute("asset", a));
        return "maintenance/report";
    }

    @PostMapping("/report")
    public String report(@RequestParam String qaCode,
                         @RequestParam String issueType,
                         @RequestParam String description,
                         @RequestParam(defaultValue = "NORMAL") String priority,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            Asset asset = assetService.findByQaCode(qaCode)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị"));
            User reporter = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow();

            maintenanceService.createRequest(asset, reporter, issueType, description, priority);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu bảo trì/báo hỏng đã được gửi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/maintenance";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id,
                          @RequestParam String resolutionNote,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            if (!canResolveMaintenance(userDetails)) {
                redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên hoặc nhân viên bảo trì mới được xác nhận sản phẩm đã sửa.");
                return "redirect:/maintenance";
            }

            User resolver = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            MaintenanceRequest resolvedRequest = maintenanceService.resolve(id, resolutionNote, resolver);
            auditLogService.logMaintenanceResolved(resolvedRequest, resolver);
            redirectAttributes.addFlashAttribute("success", "Đã giải quyết yêu cầu!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/maintenance";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id,
                         @RequestParam Long assigneeId,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            if (!hasRole(userDetails, "ADMIN")) {
                redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được phân công bảo trì.");
                return "redirect:/maintenance";
            }

            User actor = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            User assignee = userRepository.findById(assigneeId)
                    .filter(user -> Boolean.TRUE.equals(user.getIsActive())
                            && user.getRole() != null
                            && "MAINTENANCE".equals(user.getRole().getName()))
                    .orElseThrow(() -> new IllegalArgumentException("Người được phân công phải có quyền MAINTENANCE."));

            MaintenanceRequest assignedRequest = maintenanceService.assign(id, assignee);
            auditLogService.logMaintenanceAssigned(assignedRequest, actor);
            redirectAttributes.addFlashAttribute("success", "Đã phân công bảo trì cho " + assignee.getFullName() + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/maintenance";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam MaintenanceStatus status,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User actor = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
            maintenanceService.updateStatus(id, status, actor);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái!");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/maintenance";
    }

    private boolean hasRole(UserDetails userDetails, String roleName) {
        if (userDetails == null) {
            return false;
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> user.getRole() != null && roleName.equals(user.getRole().getName()))
                .orElse(false);
    }

    private boolean canResolveMaintenance(UserDetails userDetails) {
        return hasRole(userDetails, "ADMIN") || hasRole(userDetails, "MAINTENANCE");
    }
}
