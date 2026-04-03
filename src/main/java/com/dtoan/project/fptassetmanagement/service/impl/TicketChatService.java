package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.ChatMessage;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.repository.ChatMessageRepository;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MaintenanceService maintenanceService;
    private final UserRepository userRepository;
    private final TicketRealtimeService ticketRealtimeService;

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long ticketId) {
        return chatMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Transactional
    public ChatMessage sendMessage(MaintenanceRequest ticket, User sender, String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Tin nhắn không được để trống.");
        }
        if (!maintenanceService.canAccessTicket(ticket, sender)) {
            throw new IllegalStateException("Bạn không có quyền tham gia cuộc trò chuyện này.");
        }

        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .message(message.trim())
                .messageType("TEXT")
                .isSystem(false)
                .build());

        ticket.setLastActivityAt(LocalDateTime.now());
        maintenanceService.save(ticket);

        ticketRealtimeService.broadcastChatMessage(ticket, saved, recipients(ticket, sender));
        return saved;
    }

    @Transactional
    public ChatMessage systemMessage(MaintenanceRequest ticket, User actor, String message) {
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .ticket(ticket)
                .sender(actor)
                .message(message)
                .messageType("SYSTEM")
                .isSystem(true)
                .build());
        ticket.setLastActivityAt(LocalDateTime.now());
        maintenanceService.save(ticket);
        return saved;
    }

    private List<User> recipients(MaintenanceRequest ticket, User sender) {
        List<User> users = new ArrayList<>(maintenanceService.visibleRecipients(ticket));
        users.removeIf(user -> user != null && sender != null && sender.getId() != null && sender.getId().equals(user.getId()));
        return users;
    }
}
