package com.company.chat.listener;

import com.company.chat.event.*;
import com.company.chat.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Асинхронный слушатель chat-событий.
 */
@Component
@RequiredArgsConstructor
public class ChatEventListener {
    private final ChatWebSocketHandler wsHandler;
    private final ObjectMapper mapper;

    @Async("eventExecutor")
    @EventListener
    public void handleMessageEvent(MessageSentEvent ev) throws Exception {
        var dto = ev.getMessage();
        String json = mapper.writeValueAsString(dto);
        wsHandler.broadcast(dto.getRoomId(), new TextMessage(json));
    }

    @Async("eventExecutor")
    @EventListener
    public void handleFileEvent(FileUploadedEvent ev) throws Exception {
        var dto = ev.getMessage();
        String json = mapper.writeValueAsString(dto);
        wsHandler.broadcast(dto.getRoomId(), new TextMessage(json));
    }

    @Async("eventExecutor")
    @EventListener
    public void handleCallEvent(CallEvent ev) throws Exception {
        var call = ev.getCall();
        wsHandler.sendTo(call.getReceiverId(), new TextMessage(ev.getRawPayload()));
    }
}
