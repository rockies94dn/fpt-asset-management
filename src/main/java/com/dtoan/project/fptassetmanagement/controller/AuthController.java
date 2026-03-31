package com.dtoan.project.fptassetmanagement.controller;

import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.service.impl.PasswordResetService;
import com.dtoan.project.fptassetmanagement.service.impl.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationService registrationService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String expired,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "verify".equals(error)
                    ? "Tài khoản chưa xác minh email. Vui lòng kiểm tra hộp thư của bạn."
                    : "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) model.addAttribute("info", "Bạn đã đăng xuất thành công.");
        if (expired != null) model.addAttribute("warning", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        try {
            registrationService.register(user, confirmPassword);
            redirectAttributes.addFlashAttribute("info",
                    "Đăng ký thành công! Vui lòng kiểm tra email để xác minh tài khoản trước khi đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, RedirectAttributes redirectAttributes) {
        if (registrationService.verifyEmail(token)) {
            redirectAttributes.addFlashAttribute("info",
                    "Xác minh email thành công. Bạn có thể đăng nhập ngay bây giờ.");
        } else {
            redirectAttributes.addFlashAttribute("error",
                    "Liên kết xác minh không hợp lệ hoặc đã hết hạn.");
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.requestPasswordReset(email);
            redirectAttributes.addFlashAttribute("info",
                    "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (!passwordResetService.isResetTokenValid(token)) {
            redirectAttributes.addFlashAttribute("error",
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "redirect:/auth/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!passwordResetService.resetPassword(token, password, confirmPassword)) {
                redirectAttributes.addFlashAttribute("error",
                        "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
                return "redirect:/auth/forgot-password";
            }
            redirectAttributes.addFlashAttribute("info",
                    "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password?token=" + token;
        }
    }
}
