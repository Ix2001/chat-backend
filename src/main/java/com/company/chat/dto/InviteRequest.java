package com.company.chat.dto;

import lombok.*;
import java.util.List;

/**
 * DTO — приглашение пользователей в комнату.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteRequest {
    private Long roomId;
    private List<Long> userIds;
}
