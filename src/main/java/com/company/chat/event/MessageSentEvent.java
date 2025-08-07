package com.company.chat.event;

import com.company.chat.dto.MessageDto;
import org.springframework.context.ApplicationEvent;

/**
 * Событие: новое сообщение отправлено.
 */
public class MessageSentEvent extends ApplicationEvent {
    private final MessageDto message;
    public MessageSentEvent(Object src, MessageDto msg) {
        super(src);
        this.message = msg;
    }
    public MessageDto getMessage() { return message; }
}
