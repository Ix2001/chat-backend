package com.company.chat.dto;

import com.company.chat.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderUsername;
    private String text;
    private String type;
    private Instant createdAt;

    public static MessageDto from(Message m) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setRoomId(m.getRoom().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderUsername(m.getSender().getUsername());
        dto.setText(m.getContent());
        dto.setCreatedAt(m.getTimestamp());
        return dto;
    }
}
