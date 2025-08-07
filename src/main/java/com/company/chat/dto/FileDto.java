package com.company.chat.dto;

import lombok.*;

/**
 * DTO — файл.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    private Long id;
    private String filename;
    private String url;
}
