package com.company.chat.service;

import com.company.chat.repository.MembershipRepository;
import com.company.chat.websocket.WebSocketSessionRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Сервис исходящих WS-сообщений.
 * Завязан только на реестр сессий и репозиторий membership'ов.
 * НЕ зависит от RoomService и от самого ChatWebSocketHandler — это важно.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketPushService {

    private final WebSocketSessionRegistry registry;
    private final MembershipRepository membershipRepo;
    private final ObjectMapper objectMapper;

    public void sendTo(Long userId, Object payload) {
        WebSocketSession s = registry.get(userId);
        if (s == null || !s.isOpen()) return;
        try {
            s.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }

    public void broadcastToRoom(Long roomId, Object payload) {
        List<Long> userIds = membershipRepo.findUserIdsByRoomId(roomId);
        TextMessage msg;
        try {
            msg = new TextMessage(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (Long uid : userIds) {
            WebSocketSession s = registry.get(uid);
            if (s != null && s.isOpen()) {
                try {
                    s.sendMessage(msg);
                } catch (IOException ignored) {}
            }
        }
    }
}
