package com.company.chat.dto;

public class ChatMessageDto {
    private Long roomId;
    private String text;

    public ChatMessageDto() {}

    public ChatMessageDto(Long roomId, String text) {
        this.roomId = roomId;
        this.text = text;
    }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
