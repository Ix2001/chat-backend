package com.company.chat.repository;

import com.company.chat.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий файлов.
 */
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {}
