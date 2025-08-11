package com.company.chat.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Пользователь.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Внешний ID из Keycloak (JWT "sub") */
    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String publicKey;
}
