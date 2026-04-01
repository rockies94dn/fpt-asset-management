package com.dtoan.project.fptassetmanagement.websocket;

import com.dtoan.project.fptassetmanagement.api.ApiMapper;
import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.entity.MaintenanceRequest;
import com.dtoan.project.fptassetmanagement.entity.User;
import com.dtoan.project.fptassetmanagement.service.impl.MaintenanceService;
import com.dtoan.project.fptassetmanagement.service.impl.TicketChatService;
import com.dtoan.project.fptassetmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class TicketWebSocketController {

    private final MaintenanceService maintenanceService;
    private final TicketChatService ticketChatService;
    private final UserRepository userRepository;
    private final ApiMapper apiMapper;

    @MessageMapping("/tickets/{ticketId}/chat")
    public void sendMessage(@DestinationVariable Long ticketId,
                            @Payload ApiDtos.SendChatMessageRequest request,
                            Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        MaintenanceRequest ticket = maintenanceService.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ticket."));
        ticketChatService.sendMessage(ticket, user, request.message());
    }
}
