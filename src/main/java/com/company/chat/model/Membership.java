package com.company.chat.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Участник комнаты.
 */
@Entity
@Table(name = "memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","room_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Membership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
