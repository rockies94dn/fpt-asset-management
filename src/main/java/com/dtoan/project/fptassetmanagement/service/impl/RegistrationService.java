package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Role;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.RoleRepository;
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
public class RegistrationService {

    private static final long EMAIL_VERIFICATION_EXPIRE_HOURS = 24;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Transactional
    public void register(User rawUser, String confirmPassword) {
        String username = valueOrBlank(rawUser.getUsername());
        String fullName = valueOrBlank(rawUser.getFullName());
        String email = normalizeEmail(rawUser.getEmail());
        String password = valueOrBlank(rawUser.getPassword());

        if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ họ tên, tên đăng nhập, email và mật khẩu.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }

        Role staffRole = roleRepository.findByName("STAFF")
                .orElseThrow(() -> new IllegalStateException("Role STAFF not found"));

        User user = User.builder()
                .username(username)
                .fullName(fullName)
                .email(email)
                .phone(rawUser.getPhone())
                .password(passwordEncoder.encode(password))
                .role(staffRole)
                .isActive(false)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .emailVerificationExpiry(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRE_HOURS))
                .build();

        userRepository.save(user);
        sendVerificationEmail(user);
    }

    @Transactional
    public boolean verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        User user = userRepository.findByEmailVerificationToken(token).orElse(null);
        if (user == null || user.getEmailVerificationExpiry() == null) {
            return false;
        }
        if (user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setIsActive(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);
        return true;
    }

    private void sendVerificationEmail(User user) {
        if (mailFrom == null || mailFrom.isBlank() || mailFrom.endsWith("@fptasset.local")) {
            throw new IllegalStateException("Chưa cấu hình email gửi xác minh. Hãy thiết lập MAIL_USERNAME và MAIL_PASSWORD.");
        }

        String verificationUrl = buildVerificationUrl(user.getEmailVerificationToken());
        String subject = "Xác minh email đăng ký - FPT Asset Management";
        String html = buildVerificationEmail(user, verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new IllegalStateException("Không thể gửi email xác minh lúc này. Vui lòng kiểm tra cấu hình SMTP.", ex);
        }
    }

    private String buildVerificationUrl(String token) {
        String normalizedBaseUrl = appBaseUrl.endsWith("/")
                ? appBaseUrl.substring(0, appBaseUrl.length() - 1)
                : appBaseUrl;
        return normalizedBaseUrl + "/auth/verify?token=" + token;
    }

    private String buildVerificationEmail(User user, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:#f4f6fb;font-family:Inter,Segoe UI,Arial,sans-serif;color:#1f2937;">
                    <div style="max-width:640px;margin:32px auto;padding:0 16px;">
                        <div style="background:linear-gradient(135deg,#ff6b00,#ff8f3d);border-radius:24px 24px 0 0;padding:32px 36px;color:#fff;">
                            <div style="font-size:13px;letter-spacing:.12em;text-transform:uppercase;opacity:.9;">FPT Asset Management</div>
                            <h1 style="margin:12px 0 0;font-size:28px;line-height:1.25;">Xác minh email của bạn</h1>
                            <p style="margin:12px 0 0;font-size:15px;line-height:1.7;max-width:460px;">
                                Xin chào %s, cảm ơn bạn đã đăng ký tài khoản. Hãy xác minh email để kích hoạt quyền truy cập vào hệ thống.
                            </p>
                        </div>
                        <div style="background:#ffffff;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 24px 24px;padding:32px 36px;">
                            <p style="margin:0 0 16px;font-size:15px;line-height:1.7;">
                                Nhấn vào nút bên dưới để hoàn tất đăng ký. Liên kết này có hiệu lực trong <strong>24 giờ</strong>.
                            </p>
                            <div style="margin:28px 0;">
                                <a href="%s" style="display:inline-block;background:#ff6b00;color:#ffffff;text-decoration:none;font-weight:700;font-size:15px;padding:14px 28px;border-radius:999px;">
                                    Xác minh email
                                </a>
                            </div>
                            <p style="margin:0 0 12px;font-size:14px;color:#4b5563;line-height:1.7;">
                                Nếu nút không hoạt động, hãy sao chép liên kết này vào trình duyệt:
                            </p>
                            <div style="word-break:break-all;background:#f9fafb;border:1px dashed #d1d5db;border-radius:14px;padding:14px 16px;font-size:13px;color:#374151;">
                                %s
                            </div>
                            <p style="margin:24px 0 0;font-size:13px;color:#6b7280;line-height:1.7;">
                                Nếu bạn không thực hiện đăng ký này, bạn có thể bỏ qua email. Tài khoản sẽ không được kích hoạt nếu chưa xác minh.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(user.getFullName()), verificationUrl, verificationUrl);
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
