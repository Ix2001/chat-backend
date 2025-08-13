package com.company.chat.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true)
    private String username;

    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    /** BCrypt-хэш пароля */
    @Column(nullable = false)
    private String password;

    /** Роли через запятую, напр. "USER" или "USER,ADMIN" */
    @Column(nullable = false)
    private String roles;
}
