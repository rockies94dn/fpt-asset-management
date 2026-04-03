package com.dtoan.project.fptassetmanagement.web;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.service.AssetService;
import com.dtoan.project.fptassetmanagement.service.impl.RegistrationService;
import com.dtoan.project.fptassetmanagement.util.QRCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class AppWebController {

    private final AssetService assetService;
    private final QRCodeUtil qrCodeUtil;
    private final RegistrationService registrationService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/index.html";
    }

    @GetMapping("/auth/login")
    public String loginRoute() {
        return "redirect:/#/login";
    }

    @GetMapping("/auth/register")
    public String registerRoute() {
        return "redirect:/#/register";
    }

    @GetMapping("/auth/forgot-password")
    public String forgotPasswordRoute() {
        return "redirect:/#/forgot-password";
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordRoute(@RequestParam String token) {
        return "redirect:/#/reset-password?token=" + UriUtils.encodeQueryParam(token, StandardCharsets.UTF_8);
    }

    @GetMapping("/auth/verify")
    public String verifyEmail(@RequestParam String token) {
        boolean verified = registrationService.verifyEmail(token);
        return "redirect:/#/login?verified=" + (verified ? "success" : "invalid");
    }

    @GetMapping("/assets/scan/{qaCode}")
    public String scanRedirect(@PathVariable String qaCode) {
        return "redirect:/#/scan?qaCode=" + UriUtils.encodeQueryParam(qaCode, StandardCharsets.UTF_8);
    }

    @GetMapping("/assets/{id}/qr/download")
    public ResponseEntity<byte[]> downloadQr(@PathVariable Long id) {
        try {
            Asset asset = assetService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị."));
            byte[] qr = qrCodeUtil.generateQRCodeBytes(qrCodeUtil.buildAssetQRContent(asset.getQaCode()), 400, 400);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"QR_" + asset.getQaCode() + ".png\"")
                    .body(qr);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
