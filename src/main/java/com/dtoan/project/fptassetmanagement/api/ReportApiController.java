package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.repository.RoomRepository;
import com.dtoan.project.fptassetmanagement.service.impl.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final ExportService exportService;
    private final RoomRepository roomRepository;
    private final ApiMapper apiMapper;

    @GetMapping("/meta")
    public List<ApiDtos.RoomDto> meta() {
        return roomRepository.findByIsActiveTrueOrderByCodeAsc().stream().map(apiMapper::toRoomDto).toList();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam(required = false) Long roomId) throws Exception {
        byte[] data = exportService.exportAssetsToExcel(roomId);
        String filename = "ThietBi_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Long roomId) throws Exception {
        byte[] data = exportService.exportAssetsToPdf(roomId);
        String filename = "ThietBi_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(data);
    }
}
