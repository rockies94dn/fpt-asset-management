package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.entity.*;
import com.dtoan.project.fptassetmanagement.enums.UsageStatus;
import com.dtoan.project.fptassetmanagement.repository.*;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.AssetUsageService;
import com.dtoan.project.fptassetmanagement.service.impl.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usages")
@RequiredArgsConstructor
public class UsageController {

    private final AssetUsageService usageService;
    private final AssetService assetService;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomService roomService;

    @GetMapping("")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) UsageStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 15);
        model.addAttribute("usages", usageService.searchUsages(keyword, status, pageable));
        model.addAttribute("statuses", UsageStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        return "usage/list";
    }

    @GetMapping("/checkin")
    public String checkInForm(@RequestParam(required = false) String qaCode, Model model) {
        model.addAttribute("qaCode", qaCode);
        model.addAttribute("rooms", roomService.getAssignableRooms());
        if (qaCode != null && !qaCode.isBlank()) {
            assetService.findByQaCode(qaCode).ifPresent(a -> model.addAttribute("asset", a));
        }
        return "usage/checkin";
    }

    @PostMapping("/checkin")
    public String checkIn(@RequestParam String qaCode,
                          @RequestParam(required = false) Long roomToId,
                          @RequestParam(required = false) String purpose,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            Asset asset = assetService.findByQaCode(qaCode)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị: " + qaCode));
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (roomService.isStoreRoomId(roomToId)) {
                throw new IllegalArgumentException("Không thể chuyển thiết bị vào kho bằng thao tác người dùng.");
            }
            Room room = roomToId != null ? roomRepository.findById(roomToId).orElse(null) : null;

            usageService.checkIn(asset, user, room, purpose);
            redirectAttributes.addFlashAttribute("success", "Check-in thành công cho thiết bị: " + asset.getName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usages";
    }

    @PostMapping("/{id}/checkout")
    public String checkOut(@PathVariable Long id,
                           @RequestParam(required = false) String note,
                           RedirectAttributes redirectAttributes) {
        try {
            usageService.checkOut(id, note);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Check-out thành công! Thiết bị đã được trả về " + roomService.getOrCreateStoreRoom().getName() + "."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usages";
    }
}
