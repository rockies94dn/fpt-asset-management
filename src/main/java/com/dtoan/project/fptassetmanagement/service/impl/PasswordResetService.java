package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final long RESET_LINK_EXPIRE_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Transactional
    public void requestPasswordReset(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            return;
        }

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        user.setPasswordResetToken(UUID.randomUUID().toString());
        user.setPasswordResetExpiry(LocalDateTime.now().plusMinutes(RESET_LINK_EXPIRE_MINUTES));
        userRepository.save(user);

        sendResetEmail(user);
    }

    @Transactional(readOnly = true)
    public boolean isResetTokenValid(String token) {
        return getResettableUser(token) != null;
    }

    @Transactional
    public boolean resetPassword(String token, String password, String confirmPassword) {
        User user = getResettableUser(token);
        if (user == null) {
            return false;
        }

        String normalizedPassword = valueOrBlank(password);
        if (normalizedPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống.");
        }
        if (!normalizedPassword.equals(valueOrBlank(confirmPassword))) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        user.setPassword(passwordEncoder.encode(normalizedPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
        return true;
    }

    private User getResettableUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        User user = userRepository.findByPasswordResetToken(token).orElse(null);
        if (user == null || user.getPasswordResetExpiry() == null) {
            return null;
        }
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            return null;
        }
        return user;
    }

    private void sendResetEmail(User user) {
        if (mailFrom == null || mailFrom.isBlank() || mailFrom.endsWith("@fptasset.local")) {
            throw new IllegalStateException("Chưa cấu hình email gửi quên mật khẩu. Hãy thiết lập MAIL_USERNAME và MAIL_PASSWORD.");
        }

        String resetUrl = buildResetUrl(user.getPasswordResetToken());
        String subject = "Đặt lại mật khẩu - FPT Asset Management";
        String html = buildResetEmail(user, resetUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new IllegalStateException("Không thể gửi email đặt lại mật khẩu lúc này. Vui lòng kiểm tra cấu hình SMTP.", ex);
        }
    }

    private String buildResetUrl(String token) {
        String normalizedBaseUrl = appBaseUrl.endsWith("/")
                ? appBaseUrl.substring(0, appBaseUrl.length() - 1)
                : appBaseUrl;
        return normalizedBaseUrl + "/auth/reset-password?token=" + token;
    }

    private String buildResetEmail(User user, String resetUrl) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:Inter,Segoe UI,Arial,sans-serif;color:#1f2937;">
                    <div style="max-width:640px;margin:32px auto;padding:0 16px;">
                        <div style="background:linear-gradient(135deg,#0f766e,#14b8a6);border-radius:24px 24px 0 0;padding:32px 36px;color:#fff;">
                            <div style="font-size:13px;letter-spacing:.12em;text-transform:uppercase;opacity:.9;">FPT Asset Management</div>
                            <h1 style="margin:12px 0 0;font-size:28px;line-height:1.25;">Đặt lại mật khẩu</h1>
                            <p style="margin:12px 0 0;font-size:15px;line-height:1.7;max-width:460px;">
                                Xin chào %s, chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                            </p>
                        </div>
                        <div style="background:#ffffff;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 24px 24px;padding:32px 36px;">
                            <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">
                                Nhấn nút bên dưới để tạo mật khẩu mới. Liên kết này chỉ có hiệu lực trong <strong>30 phút</strong>.
                            </p>
                            <div style="margin:28px 0;">
                                <a href="%s" style="display:inline-block;background:#0f766e;color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;padding:14px 28px;border-radius:999px;">
                                    Đặt lại mật khẩu
                                </a>
                            </div>
                            <p style="margin:0 0 12px;font-size:14px;color:#4b5563;line-height:1.7;">
                                Nếu nút không hoạt động, hãy sao chép liên kết này vào trình duyệt:
                            </p>
                            <div style="word-break:break-all;background:#f9fafb;border:1px dashed #d1d5db;border-radius:14px;padding:14px 16px;font-size:13px;color:#374151;">
                                %s
                            </div>
                            <p style="margin:24px 0 0;font-size:13px;color:#6b7280;line-height:1.7;">
                                Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này. Mật khẩu hiện tại của bạn sẽ không thay đổi.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(user.getFullName()), resetUrl, resetUrl);
    }

    private String normalizeEmail(String email) {
        return valueOrBlank(email).toLowerCase(Locale.ROOT);
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeHtml(String value) {
        return valueOrBlank(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
