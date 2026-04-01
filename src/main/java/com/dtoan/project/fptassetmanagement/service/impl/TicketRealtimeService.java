package com.dtoan.project.fptassetmanagement.service.impl;

import com.dtoan.project.fptassetmanagement.api.ApiMapper;
import com.dtoan.project.fptassetmanagement.entity.ChatMessage;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TicketRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final ApiMapper apiMapper;

    public void broadcastTicketChanged(MaintenanceRequest ticket,
                                       String eventType,
                                       String message,
                                       Collection<User> recipients) {
        messagingTemplate.convertAndSend(
                "/topic/tickets/" + ticket.getId(),
                Map.of(
                        "eventType", eventType,
                        "message", message,
                        "ticket", apiMapper.toTicketDto(ticket)
                )
        );

        notificationService.pushNotification(
                recipients,
                eventType.toLowerCase() + "-ticket-" + ticket.getId(),
                "Ticket " + ticket.getTicketCode(),
                message,
                "/tickets/" + ticket.getId(),
                "bi-tools",
                toneForEvent(eventType)
        );
    }

    public void broadcastChatMessage(MaintenanceRequest ticket,
                                     ChatMessage chatMessage,
                                     Collection<User> recipients) {
        messagingTemplate.convertAndSend(
                "/topic/tickets/" + ticket.getId(),
                Map.of(
                        "eventType", "CHAT_MESSAGE",
                        "ticketId", ticket.getId(),
                        "message", apiMapper.toChatMessageDto(chatMessage)
                )
        );

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

    public Collection<User> distinctUsers(Collection<User> users) {
        Map<Long, User> indexed = new java.util.LinkedHashMap<>();
        for (User user : users) {
            if (user != null && user.getId() != null) {
                indexed.putIfAbsent(user.getId(), user);
            }
        }
        return new HashSet<>(indexed.values());
    }

    private String toneForEvent(String eventType) {
        return switch (eventType) {
            case "TICKET_OVERDUE" -> "danger";
            case "TICKET_RESOLVED" -> "success";
            case "TICKET_ASSIGNED" -> "warning";
            default -> "info";
        };
    }
}
