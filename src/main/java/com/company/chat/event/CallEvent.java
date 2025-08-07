package com.company.chat.event;

import com.company.chat.dto.CallSessionDto;
import org.springframework.context.ApplicationEvent;

/**
 * Событие: изменение статуса звонка.
 */
public class CallEvent extends ApplicationEvent {
    private final CallSessionDto call;
    private final String rawPayload;
    public CallEvent(Object src, CallSessionDto call, String rawPayload) {
        super(src);
        this.call = call;
        this.rawPayload = rawPayload;
    }
    public CallSessionDto getCall() { return call; }
    public String getRawPayload() { return rawPayload; }
}
