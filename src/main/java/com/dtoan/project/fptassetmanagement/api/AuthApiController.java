package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.service.impl.CurrentUserService;
import com.dtoan.project.fptassetmanagement.service.impl.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final PasswordResetService passwordResetService;

    @GetMapping("/me")
    public ApiDtos.MeResponse me(Authentication authentication,
                                 CsrfToken csrfToken,
                                 HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return new ApiDtos.MeResponse(false, csrfToken != null ? csrfToken.getToken() : null, null);
        }
        try {
            User user = currentUserService.requireUser(authentication);
            return new ApiDtos.MeResponse(true, csrfToken != null ? csrfToken.getToken() : null, apiMapper.toUserDto(user));
        } catch (IllegalStateException exception) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();
            return new ApiDtos.MeResponse(false, csrfToken != null ? csrfToken.getToken() : null, null);
        }
    }

    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiDtos.MeResponse login(@RequestBody ApiDtos.AuthRequest request,
                                    HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    CsrfToken csrfToken) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        User user = currentUserService.requireUser(authentication);
        return new ApiDtos.MeResponse(true, csrfToken != null ? csrfToken.getToken() : null, apiMapper.toUserDto(user));
    }

    @PostMapping("/auth/logout")
    public ApiDtos.SimpleMessageResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new ApiDtos.SimpleMessageResponse("Đăng xuất thành công.");
    }

    @PostMapping("/auth/forgot-password")
    public ApiDtos.SimpleMessageResponse forgotPassword(@RequestBody ApiDtos.ForgotPasswordRequest request) {
        passwordResetService.requestPasswordReset(request.email());
        return new ApiDtos.SimpleMessageResponse(
                "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu."
        );
    }

    @GetMapping("/auth/reset-password/validate")
    public ApiDtos.TokenValidationResponse validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.isResetTokenValid(token);
        return new ApiDtos.TokenValidationResponse(
                valid,
                valid
                        ? "Liên kết đặt lại mật khẩu hợp lệ."
                        : "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
        );
    }

    @PostMapping("/auth/reset-password")
    public ApiDtos.SimpleMessageResponse resetPassword(@RequestBody ApiDtos.ResetPasswordConfirmRequest request) {
        boolean reset = passwordResetService.resetPassword(
                request.token(),
                request.password(),
                request.confirmPassword()
        );
        if (!reset) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn."
            );
        }
        return new ApiDtos.SimpleMessageResponse("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới.");
    }
}
