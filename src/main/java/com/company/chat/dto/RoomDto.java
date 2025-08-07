package com.company.chat.dto;

import lombok.*;

/**
 * DTO — Комната/Диалог.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private Long id;
    private String name;
    private String type;
}
