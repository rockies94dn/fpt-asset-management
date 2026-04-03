package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.entity.ChatMessage;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public void broadcastTicketChanged(MaintenanceRequest ticket,
                                       String eventType,
                                       String message,
                                       Collection<User> notificationRecipients,
                                       Collection<User> refreshRecipients) {
        pushTicketRefresh(refreshRecipients, Map.of(
                "eventType", eventType,
                "ticketId", ticket.getId(),
                "message", message
        ));

        notificationService.pushNotification(
                notificationRecipients,
                eventType.toLowerCase() + "-ticket-" + ticket.getId(),
                "Ticket " + ticket.getTicketCode(),
                message,
                "/tickets/" + ticket.getId(),
                "bi-tools",
                toneForEvent(eventType)
        );
    }

    public void broadcastTicketChanged(MaintenanceRequest ticket,
                                       String eventType,
                                       String message,
                                       Collection<User> recipients) {
        broadcastTicketChanged(ticket, eventType, message, recipients, recipients);
    }

    public void broadcastChatMessage(MaintenanceRequest ticket,
                                     ChatMessage chatMessage,
                                     Collection<User> recipients) {
        pushTicketRefresh(recipients, Map.of(
                "eventType", "CHAT_MESSAGE",
                "ticketId", ticket.getId(),
                "message", chatMessage.getMessage()
        ));

        notificationService.pushNotification(
                recipients,
                "chat-ticket-" + ticket.getId(),
                "Tin nhắn mới trong " + ticket.getTicketCode(),
                chatMessage.getSender().getFullName() + ": " + chatMessage.getMessage(),
                "/tickets/" + ticket.getId(),
                "bi-chat-dots",
                "primary"
        );
    }

    private void pushTicketRefresh(Collection<User> recipients, Map<String, Object> payload) {
        if (recipients == null) {
            return;
        }
        for (User user : recipients) {
            if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
                continue;
            }
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/tickets", payload);
        }
    }

    private String toneForEvent(String eventType) {
        return switch (eventType) {
            case "TICKET_OVERDUE" -> "danger";
            case "TICKET_RESOLVED" -> "success";
            case "TICKET_ASSIGNED", "TICKET_CLAIMED" -> "warning";
            default -> "info";
        };
    }
}
