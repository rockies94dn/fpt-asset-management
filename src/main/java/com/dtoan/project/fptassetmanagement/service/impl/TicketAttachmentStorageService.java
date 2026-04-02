package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.TicketAttachment;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.TicketAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketAttachmentStorageService {

    @Value("${app.upload.ticket-root:uploads/tickets}")
    private String uploadRoot;

    private final TicketAttachmentRepository attachmentRepository;

    @Transactional
    public TicketAttachment store(MaintenanceRequest ticket, User uploadedBy, MultipartFile file) throws IOException {
        validateFile(file);

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path ticketDirectory = Path.of(uploadRoot, String.valueOf(ticket.getId())).toAbsolutePath().normalize();
        Files.createDirectories(ticketDirectory);

        Path target = ticketDirectory.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        TicketAttachment attachment = TicketAttachment.builder()
                .ticket(ticket)
                .uploadedBy(uploadedBy)
                .originalName(originalName)
                .storedName(storedName)
                .relativePath(Path.of(String.valueOf(ticket.getId()), storedName).toString())
                .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
                .size(file.getSize())
                .build();

        return attachmentRepository.save(attachment);
    }

    @Transactional(readOnly = true)
    public List<TicketAttachment> listForTicket(Long ticketId) {
        return attachmentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional(readOnly = true)
    public TicketAttachment findById(Long ticketId, Long attachmentId) {
        return attachmentRepository.findByIdAndTicketId(attachmentId, ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp đính kèm."));
    }

    public Path resolvePath(TicketAttachment attachment) {
        return Path.of(uploadRoot).toAbsolutePath().normalize().resolve(attachment.getRelativePath()).normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp đính kèm trống.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép tải lên hình ảnh.");
        }
    }
}
