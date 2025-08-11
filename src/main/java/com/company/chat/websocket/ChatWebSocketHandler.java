package com.company.chat.websocket;

import com.company.chat.dto.MessageDto;
import com.company.chat.model.CallType;
import com.company.chat.service.RoomService;
import com.company.chat.service.MessagingService;
import com.company.chat.service.CallService;
import com.company.chat.service.FileStorageService;
import com.company.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Обработчик WebSocket-сообщений:
 * - зашифрованные тексты
 * - файлы
 * - сигнализация WebRTC (звонки)
 */
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MessagingService msgSvc;
    private final RoomService roomSvc;
    private final FileStorageService fileSvc;
    private final CallService callSvc;
    private final UserService userSvc;
    private final WebSocketSessionRegistry registry;

    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession sess) {
        String username = (String)sess.getAttributes().get("username");
        Long uid = userSvc.listAll().stream()
                .filter(u->u.getUsername().equals(username))
                .findFirst().orElseThrow().getId();
        sessions.put(uid, sess);
        registry.register(username, sess);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession sess, CloseStatus status) {
        sessions.values().remove(sess);
    }

    @Override
    protected void handleTextMessage(WebSocketSession sess, TextMessage msg) throws Exception {
        JsonNode root = mapper.readTree(msg.getPayload());
        String action = root.get("action").asText();
        Long senderId = (Long) sess.getAttributes().get("userId");

        switch(action) {
            case "sendMessage":
                Long roomId = root.get("roomId").asLong();
                String content = root.get("content").asText();
                msgSvc.saveEncrypted(roomId, senderId, content);
                break;

            case "fileMessage":
                roomId = root.get("roomId").asLong();
                // assume content holds fileId
                Long fileId = Long.valueOf(root.get("fileId").asText());
                msgSvc.saveEncrypted(roomId, senderId, fileId.toString());
                break;

            case "callOffer":
                String callId = root.get("callId").asText();
                callSvc.initiate(callId,
                        CallType.valueOf(root.get("type").asText()),
                        root.get("from").asLong(),
                        root.get("to").asLong());
                break;

            case "callRing":
                callSvc.ring(root.get("callId").asText());
                break;

            case "callAnswer":
                callSvc.answer(root.get("callId").asText());
                break;

            case "callReject":
                callSvc.reject(root.get("callId").asText());
                break;

            case "iceCandidate":
                // для iceCandidate прокидывать напрямую:
                Long to = root.get("to").asLong();
                sendTo(to, new TextMessage(msg.getPayload()));
                break;

            case "callEnd":
                callSvc.end(root.get("callId").asText());
                break;

            default:
        }
    }

    /**
     * Отправить всем участникам комнаты.
     */
    public void broadcast(Long roomId, TextMessage msg) {
        roomSvc.getMemberIds(roomId).forEach(uid -> sendTo(uid, msg));
    }

    /**
     * Отправить конкретному пользователю.
     */
    public void sendTo(Long userId, TextMessage msg) {
        WebSocketSession s = sessions.get(userId);
        if (s != null && s.isOpen()) {
            try { s.sendMessage(msg); }
            catch (Exception ignored) {}
        }
    }
}
