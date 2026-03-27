package com.dtoan.project.fptassetmanagement.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class QRCodeUtil {

    @Value("${app.qr.size:300}")
    private int qrSize;

    @Value("${app.qr.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String QR_DIR = "qrcodes";

    public String generateQRCodeBase64(String content) throws WriterException, IOException {
        byte[] qrBytes = generateQRCodeBytes(content, qrSize, qrSize);
        return Base64.getEncoder().encodeToString(qrBytes);
    }

    public byte[] generateQRCodeBytes(String content, int width, int height) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return baos.toByteArray();
    }

    public String saveQRCodeToFile(String qaCode, String content) throws WriterException, IOException {
        Path dirPath = Paths.get("uploads", QR_DIR);
        Files.createDirectories(dirPath);

        String fileName = qaCode.replace("/", "-") + ".png";
        Path filePath = dirPath.resolve(fileName);

        byte[] qrBytes = generateQRCodeBytes(content, qrSize, qrSize);
        Files.write(filePath, qrBytes);

        return "/uploads/qrcodes/" + fileName;
    }

    public String buildAssetQRContent(String qaCode) {
        return baseUrl + "/assets/scan/" + qaCode;
    }

    public String generateQRCodeWithLabel(String content, String label) throws WriterException, IOException {
        int qrWidth = 280;
        int qrHeight = 280;
        int labelHeight = 40;
        int totalHeight = qrHeight + labelHeight;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        BufferedImage finalImage = new BufferedImage(qrWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = finalImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, qrWidth, totalHeight);
        g2d.drawImage(qrImage, 0, 0, null);

        g2d.setColor(new Color(0xFF6B00)); // FPT Orange
        g2d.fillRect(0, qrHeight, qrWidth, labelHeight);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (qrWidth - fm.stringWidth(label)) / 2;
        g2d.drawString(label, x, qrHeight + 26);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(finalImage, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}