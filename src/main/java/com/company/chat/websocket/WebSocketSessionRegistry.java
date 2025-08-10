package com.company.chat.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

/**
 * Реестр WS-сессий: userId -> session.
 * Не зависит ни от сервисов домена, ни от RoomService.
 */
@Component
public class WebSocketSessionRegistry {

    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    public void remove(Long userId) {
        sessions.remove(userId);
    }

    public WebSocketSession get(Long userId) {
        return sessions.get(userId);
    }
}
