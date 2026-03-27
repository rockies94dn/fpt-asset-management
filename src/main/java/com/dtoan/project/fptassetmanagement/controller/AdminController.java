package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.entity.Room;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== USERS =====
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("currentPage", "users");
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái người dùng.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/resetPassword")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        userRepository.findById(id).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
        redirectAttributes.addFlashAttribute("success", "Đã đặt lại mật khẩu.");
        return "redirect:/admin/users";
    }

    // ===== ROOMS =====
    @GetMapping("/rooms")
    public String rooms(Model model) {
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("currentPage", "rooms");
        return "admin/rooms";
    }

    @PostMapping("/rooms/create")
    public String createRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        if (roomRepository.existsByCode(room.getCode())) {
            redirectAttributes.addFlashAttribute("error", "Mã phòng đã tồn tại!");
        } else {
            roomRepository.save(room);
            redirectAttributes.addFlashAttribute("success", "Đã thêm phòng: " + room.getName());
        }
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomRepository.findById(id).ifPresent(room -> {
            room.setIsActive(false);
            roomRepository.save(room);
        });
        redirectAttributes.addFlashAttribute("success", "Đã xóa phòng.");
        return "redirect:/admin/rooms";
    }
}