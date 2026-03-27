package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.Asset;
import com.dtoan.project.fptassetmanagement.repository.AssetRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final AssetRepository assetRepository;

    public byte[] exportAssetsToExcel(Long roomId) throws IOException {
        List<Asset> assets = roomId != null
                ? assetRepository.findByRoomId(roomId)
                : assetRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Danh sách thiết bị");

            // Styles
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xFF, (byte)0x6B, 0x00}, null));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH THIẾT BỊ - FPT POLYTECHNIC ĐÀ NẴNG");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Ngày xuất: " +
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            // Header row
            Row headerRow = sheet.createRow(3);
            String[] headers = {"STT", "Mã QA", "Tên thiết bị", "Loại", "Phòng",
                    "Thương hiệu", "Model", "Trạng thái", "Ngày mua"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Data rows
            XSSFCellStyle altStyle = workbook.createCellStyle();
            altStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0xFF, (byte)0xF3, (byte)0xE0}, null));
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (int i = 0; i < assets.size(); i++) {
                Asset a = assets.get(i);
                Row row = sheet.createRow(i + 4);
                if (i % 2 == 1) {
                    for (int j = 0; j < headers.length; j++) row.createCell(j).setCellStyle(altStyle);
                }
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(a.getQaCode());
                row.createCell(2).setCellValue(a.getName());
                row.createCell(3).setCellValue(a.getCategory() != null ? a.getCategory().getName() : "");
                row.createCell(4).setCellValue(a.getRoom() != null ? a.getRoom().getName() : "");
                row.createCell(5).setCellValue(a.getBrand() != null ? a.getBrand() : "");
                row.createCell(6).setCellValue(a.getModel() != null ? a.getModel() : "");
                row.createCell(7).setCellValue(a.getStatus().getDisplayName());
                row.createCell(8).setCellValue(a.getPurchaseDate() != null ? a.getPurchaseDate().format(dateFormatter) : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportAssetsToPdf(Long roomId) throws Exception {
        List<Asset> assets = roomId != null
                ? assetRepository.findByRoomId(roomId)
                : assetRepository.findAll();

        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,
                new BaseColor(0xFF, 0x6B, 0x00));
        Paragraph title = new Paragraph("DANH SACH THIET BI - FPT POLYTECHNIC DA NANG", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        Paragraph date = new Paragraph("Ngay xuat: " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), subFont);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(15);
        document.add(date);

        // Table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 1.5f, 2.5f, 1.5f, 1.5f, 1.2f, 1.2f, 1.1f});

        BaseColor headerColor = new BaseColor(0xFF, 0x6B, 0x00);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        String[] headers = {"STT", "Ma QA", "Ten thiet bi", "Loai", "Phong", "Thuong hieu", "Model", "Trang thai"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 9);
        BaseColor altColor = new BaseColor(0xFF, 0xF3, 0xE0);

        for (int i = 0; i < assets.size(); i++) {
            Asset a = assets.get(i);
            BaseColor rowColor = (i % 2 == 1) ? altColor : BaseColor.WHITE;

            String[] values = {
                    String.valueOf(i + 1), a.getQaCode(), a.getName(),
                    a.getCategory() != null ? a.getCategory().getName() : "",
                    a.getRoom() != null ? a.getRoom().getName() : "",
                    a.getBrand() != null ? a.getBrand() : "",
                    a.getModel() != null ? a.getModel() : "",
                    a.getStatus().getDisplayName()
            };

            for (String v : values) {
                PdfPCell cell = new PdfPCell(new Phrase(v, dataFont));
                cell.setBackgroundColor(rowColor);
                cell.setPadding(5);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }
}