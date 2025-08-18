package com.company.chat.controller;

import com.company.chat.dto.ChatMessageDto;
import com.company.chat.dto.MessageDto;
import com.company.chat.repository.UserRepository;
import com.company.chat.service.MessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessagingService messageService;   // сохраняет Message в БД
    private final SimpMessagingTemplate broker;      // рассылает
    private final UserRepository userRepo;           // НУЖЕН, чтобы получить id отправителя

    @MessageMapping("/rooms/{roomId}/send")
    public void sendToRoom(@DestinationVariable Long roomId,
                           ChatMessageDto payload,   // { "text": "..." }
                           Principal principal) {

        // principal.getName() -> username
        var sender = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Sender not found: " + principal.getName()));

        var saved = messageService.saveEncryptedEntity(roomId, sender.getId(), payload.getText());

        // Отправляем всем подписчикам комнаты
        broker.convertAndSend("/topic/rooms." + roomId, MessageDto.from(saved));
    }
}
