package com.company.chat.dto;

import lombok.*;

/**
 * DTO — Public Key пользователя.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicKeyDto {
    private Long userId;
    private String publicKey;
}
