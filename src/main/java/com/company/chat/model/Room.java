package com.company.chat.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Комната или диалог.
 */
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    private String name;
}
