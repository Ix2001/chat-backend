package com.company.chat.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Сессия звонка.
 */
@Entity
@Table(name = "call_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallType type;

    @ManyToOne @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;

    @Column(nullable = false)
    private Instant startTime;

    private Instant answerTime;
    private Instant endTime;
}
