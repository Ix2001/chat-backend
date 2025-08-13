package com.company.chat.dto;

import lombok.*;
import java.time.Instant;

/**
 * DTO — сообщение.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private Long roomId;
    private String type;
    private String content;
    private Instant timestamp;
}
