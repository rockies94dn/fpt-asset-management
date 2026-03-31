    package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.enums.AssetStatus;
import com.dtoan.project.fptassetmanagement.enums.MaintenanceStatus;
import com.dtoan.project.fptassetmanagement.repository.AssetCategoryRepository;
import com.dtoan.project.fptassetmanagement.repository.MaintenanceRequestRepository;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import com.dtoan.project.fptassetmanagement.service.AssetService;
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

    private final AssetService assetService;
    private final AssetCategoryRepository categoryRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) AssetStatus status,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long roomId,
                       @RequestParam(defaultValue = "false") boolean attentionOnly,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
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
        addMaintenanceAttention(model, assets.getContent());
        return "asset/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("asset", new Asset());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("rooms", roomRepository.findByIsActiveTrueOrderByCodeAsc());
        model.addAttribute("statuses", AssetStatus.values());
        return "asset/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Asset asset,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long roomId,
                         @RequestParam(required = false, defaultValue = "false") boolean autoGenerateCode,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            categoryRepository.findById(categoryId).ifPresent(asset::setCategory);
            roomRepository.findById(roomId).ifPresent(asset::setRoom);

            if (autoGenerateCode || asset.getQaCode() == null || asset.getQaCode().isBlank()) {
                String catCode = asset.getCategory() != null
                        ? asset.getCategory().getName().substring(0, Math.min(3, asset.getCategory().getName().length())).toUpperCase()
                        : "GEN";
                asset.setQaCode(assetService.generateQaCode(catCode));
            }

            userRepository.findByUsername(userDetails.getUsername()).ifPresent(asset::setCreatedBy);
            Asset saved = assetService.save(asset);
            redirectAttributes.addFlashAttribute("success", "Thêm thiết bị thành công! Mã QA: " + saved.getQaCode());
            return "redirect:/assets/" + saved.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/assets/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        model.addAttribute("asset", asset);
        model.addAttribute("openMaintenanceRequest",
                maintenanceRequestRepository.findFirstByAssetIdAndStatusInOrderByReportedAtDesc(
                        asset.getId(),
                        OPEN_MAINTENANCE_STATUSES
                ).orElse(null));
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
    public String editForm(@PathVariable Long id, Model model) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));
        model.addAttribute("asset", asset);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("rooms", roomRepository.findByIsActiveTrueOrderByCodeAsc());
        model.addAttribute("statuses", AssetStatus.values());
        return "asset/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Asset assetForm,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long roomId,
                         RedirectAttributes redirectAttributes) {
        Asset asset = assetService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        asset.setName(assetForm.getName());
        asset.setStatus(assetForm.getStatus());
        asset.setBrand(assetForm.getBrand());
        asset.setModel(assetForm.getModel());
        asset.setSerialNumber(assetForm.getSerialNumber());
        asset.setPurchaseDate(assetForm.getPurchaseDate());
        asset.setPurchasePrice(assetForm.getPurchasePrice());
        asset.setWarrantyExpiry(assetForm.getWarrantyExpiry());
        asset.setDescription(assetForm.getDescription());

        categoryRepository.findById(categoryId).ifPresent(asset::setCategory);
        roomRepository.findById(roomId).ifPresent(asset::setRoom);

        assetService.save(asset);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thiết bị thành công!");
        return "redirect:/assets/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        assetService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Đã xóa thiết bị.");
        return "redirect:/assets";
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
}
