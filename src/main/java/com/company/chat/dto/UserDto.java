package com.company.chat.dto;

import lombok.*;

/**
 * DTO — Пользователь.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String displayName;
    private String publicKey;
}
