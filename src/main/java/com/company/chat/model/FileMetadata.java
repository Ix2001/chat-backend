package com.company.chat.model;


import jakarta.persistence.*;
import lombok.*;

/**
 * Метаданные загруженного файла.
 */
@Entity
@Table(name = "files")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FileMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private String contentType;
    private Long size;
    private String storagePath;
}
