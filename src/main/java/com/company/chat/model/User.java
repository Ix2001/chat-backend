package com.company.chat.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Локальный пользователь. Ключевое поле — username (из JWT).
 */
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный логин из токена (preferred_username и т.п.). */
    @Column(nullable = false, unique = true)
    private String username;

    /** Отображаемое имя (name / given_name + family_name). */
    private String displayName;

    /** Публичный ключ для E2E (если используете). */
    @Column(columnDefinition = "TEXT")
    private String publicKey;
}
