    package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetCategoryRepository;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.AuditLogService;
import com.dtoan.project.fptassetmanagement.service.impl.AssetUsageService;
import com.dtoan.project.fptassetmanagement.service.impl.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private static final int OVERDUE_MAINTENANCE_DAYS = 7;
    private static final List<MaintenanceStatus> OPEN_MAINTENANCE_STATUSES =
            List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS);

    private final AssetRepository assetRepository;
    private final AssetService assetService;
    private final AssetCategoryRepository categoryRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final RoomService roomService;
    private final AuditLogService auditLogService;
    private final AssetUsageService assetUsageService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) AssetStatus status,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long roomId,
                       @RequestParam(defaultValue = "false") boolean attentionOnly,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Asset> assets = assetService.searchAssets(keyword, status, categoryId, roomId, attentionOnly, pageable);
        model.addAttribute("assets", assets);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("rooms", roomRepository.findByIsActiveTrueOrderByCodeAsc());
        model.addAttribute("statuses", AssetStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedRoomId", roomId);
        model.addAttribute("attentionOnly", attentionOnly);
        model.addAttribute("canDeleteAsset", isAdmin(userDetails));
        addMaintenanceAttention(model, assets.getContent());
        return "asset/list";
    }

    @GetMapping("/create")
    public String createForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("asset", new Asset());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("storeRoom", roomService.getOrCreateStoreRoom());
        model.addAttribute("statuses", AssetStatus.values());
        model.addAttribute("canManageAssetApproval", isAdmin(userDetails));
        return "asset/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Asset asset,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false, defaultValue = "false") boolean autoGenerateCode,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.findById(categoryId).ifPresent(asset::setCategory);
            asset.setRoom(roomService.getOrCreateStoreRoom());

            if (autoGenerateCode || asset.getQaCode() == null || asset.getQaCode().isBlank()) {
                String catCode = asset.getCategory() != null
                        ? asset.getCategory().getName().substring(0, Math.min(3, asset.getCategory().getName().length())).toUpperCase()
                        : "GEN";
                asset.setQaCode(assetService.generateQaCode(catCode));
            }

            userRepository.findByUsername(userDetails.getUsername()).ifPresent(asset::setCreatedBy);
            if (!isAdmin(userDetails)) {
                asset.setStatus(AssetStatus.AVAILABLE);
            }
            Asset saved = assetService.save(asset);
            userRepository.findByUsername(userDetails.getUsername())
                    .ifPresent(actor -> auditLogService.logAssetCreated(saved, actor));
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Thêm thiết bị thành công! Mã QA: " + saved.getQaCode() + ". Thiết bị đã được đưa vào "
                            + saved.getRoom().getName() + "."
            );
            return "redirect:/assets/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/assets/create";
        }
    }

    @GetMapping("/report-lost")
    public String reportLostForm(@RequestParam(required = false) String qaCode,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (!isAdmin(userDetails)) {
            return "redirect:/dashboard";
        }

        String resolvedQaCode = (qaCode != null && !qaCode.isBlank())
                ? qaCode
                : Objects.toString(model.asMap().get("qaCode"), null);
        model.addAttribute("assetsForLostReport", assetRepository.findByIsActiveTrueOrderByNameAsc());
        model.addAttribute("qaCode", resolvedQaCode);
        if (resolvedQaCode != null && !resolvedQaCode.isBlank()) {
            assetService.findByQaCode(resolvedQaCode).ifPresent(asset -> model.addAttribute("asset", asset));
        }
        return "asset/report-lost";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @RequestParam(defaultValue = "0") int auditPage,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        Pageable auditLogPageable = PageRequest.of(Math.max(auditPage, 0), 5);
        model.addAttribute("asset", asset);
        model.addAttribute("openMaintenanceRequest",
                maintenanceRequestRepository.findFirstByAssetIdAndStatusInOrderByReportedAtDesc(
                        asset.getId(),
                        OPEN_MAINTENANCE_STATUSES
                ).orElse(null));
        model.addAttribute("maintenanceHistory", maintenanceRequestRepository.findByAssetIdOrderByReportedAtDesc(asset.getId()));
        model.addAttribute("assetAuditLogs", auditLogService.getAssetAuditLogs(asset.getId(), auditLogPageable));
        model.addAttribute("selectedAuditPage", auditLogPageable.getPageNumber());
        model.addAttribute("openMaintenanceCount",
                maintenanceRequestRepository.countOpenRequestsByAssetIds(
                        List.of(asset.getId()),
                        OPEN_MAINTENANCE_STATUSES
                ).stream()
                        .findFirst()
                        .map(row -> ((Number) row[1]).longValue())
                        .orElse(0L));
        model.addAttribute("hasOverdueMaintenance",
                !maintenanceRequestRepository.findAssetIdsWithOverdueOpenRequests(
                        List.of(asset.getId()),
                        OPEN_MAINTENANCE_STATUSES,
                        LocalDateTime.now().minusDays(OVERDUE_MAINTENANCE_DAYS)
                ).isEmpty());
        model.addAttribute("canDeleteAsset", isAdmin(userDetails));
        model.addAttribute("canManageAssetApproval", isAdmin(userDetails));
        model.addAttribute("storeRoom", roomService.getOrCreateStoreRoom());
        model.addAttribute("overdueMaintenanceDays", OVERDUE_MAINTENANCE_DAYS);
        try {
            String qrBase64 = assetService.generateQRCode(asset.getQaCode());
            model.addAttribute("qrBase64", qrBase64);
        } catch (Exception e) {
            model.addAttribute("qrError", "Không thể tạo QR code");
        }
        return "asset/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        model.addAttribute("asset", asset);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("rooms", roomService.getAssignableRooms());
        model.addAttribute("statuses", AssetStatus.values());
        model.addAttribute("canEditRoom", isAdmin(userDetails));
        model.addAttribute("canDeleteAsset", isAdmin(userDetails));
        model.addAttribute("canManageAssetApproval", isAdmin(userDetails));
        return "asset/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Asset assetForm,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long roomId,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        boolean isAdmin = isAdmin(userDetails);

        asset.setName(assetForm.getName());
        asset.setBrand(assetForm.getBrand());
        asset.setModel(assetForm.getModel());
        asset.setSerialNumber(assetForm.getSerialNumber());
        asset.setPurchaseDate(assetForm.getPurchaseDate());
        asset.setPurchasePrice(assetForm.getPurchasePrice());
        asset.setWarrantyExpiry(assetForm.getWarrantyExpiry());
        asset.setDescription(assetForm.getDescription());

        categoryRepository.findById(categoryId).ifPresent(asset::setCategory);
        if (isAdmin) {
            asset.setStatus(assetForm.getStatus());
            if (roomService.isStoreRoomId(roomId)) {
                redirectAttributes.addFlashAttribute("error", "Không thể gán thủ công thiết bị vào kho.");
                return "redirect:/assets/" + id + "/edit";
            }
            roomRepository.findById(roomId).ifPresent(asset::setRoom);
        } else {
            if (assetForm.getStatus() != null && assetForm.getStatus() != asset.getStatus()) {
                redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được thay đổi trạng thái thiết bị.");
                return "redirect:/assets/" + id + "/edit";
            }
            if (roomId != null) {
                redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được gán phòng thủ công.");
                return "redirect:/assets/" + id + "/edit";
            }
        }

        assetService.save(asset);
        userRepository.findByUsername(userDetails.getUsername())
                .ifPresent(actor -> auditLogService.logAssetUpdated(asset, actor));
        redirectAttributes.addFlashAttribute("success", "Cập nhật thiết bị thành công!");
        return "redirect:/assets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(userDetails)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được xóa thiết bị.");
            return "redirect:/assets";
        }

        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        assetService.deleteById(id);
        userRepository.findByUsername(userDetails.getUsername())
                .ifPresent(actor -> auditLogService.logAssetDeleted(asset, actor));
        redirectAttributes.addFlashAttribute("success", "Đã xóa thiết bị.");
        return "redirect:/assets";
    }

    @PostMapping("/{id}/mark-lost")
    public String markLost(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin(userDetails)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được đánh dấu thất lạc.");
            return "redirect:/assets/" + id;
        }

        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        userRepository.findByUsername(userDetails.getUsername())
                .ifPresent(actor -> markAssetLost(asset, actor, null));
        redirectAttributes.addFlashAttribute("warning", buildLostSuccessMessage(asset));
        return "redirect:/assets/" + id;
    }

    @PostMapping("/report-lost")
    public String reportLost(@RequestParam String qaCode,
                             @RequestParam(required = false) String note,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(userDetails)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được báo thất lạc.");
            return "redirect:/dashboard";
        }

        try {
            Asset asset = assetService.findByQaCode(qaCode)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị với mã QA: " + qaCode));
            User actor = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            markAssetLost(asset, actor, note);
            redirectAttributes.addFlashAttribute("warning", buildLostSuccessMessage(asset));
            return "redirect:/assets/" + asset.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("qaCode", qaCode);
            return "redirect:/assets/report-lost";
        }
    }

    @PostMapping("/{id}/mark-found")
    public String markFound(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(userDetails)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ quản trị viên mới được xác nhận tìm thấy thiết bị.");
            return "redirect:/assets/" + id;
        }

        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        if (asset.getStatus() != AssetStatus.LOST) {
            redirectAttributes.addFlashAttribute("info", "Thiết bị này không ở trạng thái thất lạc.");
            return "redirect:/assets/" + id;
        }

        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setRoom(roomService.getOrCreateStoreRoom());
        assetService.save(asset);
        userRepository.findByUsername(userDetails.getUsername())
                .ifPresent(actor -> auditLogService.logAssetMarkedFound(asset, actor));

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã xác nhận tìm thấy thiết bị và chuyển thiết bị về " + asset.getRoom().getName() + "."
        );
        return "redirect:/assets/" + id;
    }

    // QR Scan endpoint - public
    @GetMapping("/scan/{qaCode}")
    public String scanQR(@PathVariable String qaCode, Model model) {
        return assetService.findByQaCode(qaCode).map(asset -> {
            model.addAttribute("asset", asset);
            return "asset/scan";
        }).orElse("error/404");
    }

    // Download QR PNG
    @GetMapping("/{id}/qr/download")
    public ResponseEntity<byte[]> downloadQR(@PathVariable Long id) {
        try {
            Asset asset = assetService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));
            String content = "/assets/scan/" + asset.getQaCode();
            // Generate raw QR bytes
            com.dtoan.project.fptassetmanagement.util.QRCodeUtil util = new com.dtoan.project.fptassetmanagement.util.QRCodeUtil();
            byte[] qr = util.generateQRCodeBytes(content, 400, 400);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"QR_" + asset.getQaCode() + ".png\"")
                    .body(qr);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void addMaintenanceAttention(Model model, List<Asset> assets) {
        if (assets.isEmpty()) {
            model.addAttribute("openMaintenanceCounts", Collections.emptyMap());
            model.addAttribute("overdueMaintenanceAssetIds", Collections.emptySet());
            return;
        }

        List<Long> assetIds = assets.stream().map(Asset::getId).toList();
        Map<Long, Long> openMaintenanceCounts = maintenanceRequestRepository
                .countOpenRequestsByAssetIds(assetIds, OPEN_MAINTENANCE_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        Set<Long> overdueMaintenanceAssetIds = new HashSet<>(
                maintenanceRequestRepository.findAssetIdsWithOverdueOpenRequests(
                        assetIds,
                        OPEN_MAINTENANCE_STATUSES,
                        LocalDateTime.now().minusDays(OVERDUE_MAINTENANCE_DAYS)
                )
        );

        model.addAttribute("openMaintenanceCounts", openMaintenanceCounts);
        model.addAttribute("overdueMaintenanceAssetIds", overdueMaintenanceAssetIds);
    }

    private void markAssetLost(Asset asset, com.dtoan.project.fptassetmanagement.entity.User actor, String note) {
        if (asset.getStatus() == AssetStatus.LOST) {
            throw new IllegalStateException("Thiết bị này đã ở trạng thái thất lạc.");
        }

        assetUsageService.closeActiveUsageForLostAsset(asset);
        asset.setStatus(AssetStatus.LOST);
        assetService.save(asset);
        auditLogService.logAssetMarkedLost(asset, actor, note);
    }

    private String buildLostSuccessMessage(Asset asset) {
        return "Đã đánh dấu thiết bị là thất lạc. Nếu đang có phiếu sử dụng mở, hệ thống đã tự đóng phiếu đó."
                + " Vị trí ghi nhận cuối: " + (asset.getRoom() != null ? asset.getRoom().getName() : "chưa xác định") + ".";
    }

    private boolean isAdmin(UserDetails userDetails) {
        return hasRole(userDetails, "ADMIN");
    }

    private boolean hasRole(UserDetails userDetails, String roleName) {
        if (userDetails == null) {
            return false;
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> user.getRole() != null && roleName.equals(user.getRole().getName()))
                .orElse(false);
    }
}
