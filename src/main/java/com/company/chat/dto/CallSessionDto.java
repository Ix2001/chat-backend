package com.company.chat.dto;

import com.company.chat.model.CallSession;
import lombok.*;
import java.time.Instant;

/**
 * DTO — сессия звонка.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSessionDto {
    private String callId;
    private String type;
    private Long initiatorId;
    private Long receiverId;
    private String status;
    private Instant startTime;
    private Instant answerTime;
    private Instant endTime;

    public static CallSessionDto fromEntity(CallSession cs) {
        return CallSessionDto.builder()
                .callId(cs.getCallId())
                .type(cs.getType().name())
                .initiatorId(cs.getInitiator().getId())
                .receiverId(cs.getReceiver().getId())
                .status(cs.getStatus().name())
                .startTime(cs.getStartTime())
                .answerTime(cs.getAnswerTime())
                .endTime(cs.getEndTime())
                .build();
    }
}
