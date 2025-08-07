package com.company.chat.event;

import com.company.chat.dto.MessageDto;
import org.springframework.context.ApplicationEvent;

/**
 * Событие: файл загружен как сообщение.
 */
public class FileUploadedEvent extends ApplicationEvent {
    private final MessageDto message;
    public FileUploadedEvent(Object src, MessageDto msg) {
        super(src);
        this.message = msg;
    }
    public MessageDto getMessage() { return message; }
}
