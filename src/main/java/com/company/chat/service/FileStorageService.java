package com.company.chat.service;

import com.company.chat.dto.MessageDto;
import com.company.chat.event.FileUploadedEvent;
import com.company.chat.mapper.FileMapper;
import com.company.chat.model.FileMetadata;
import com.company.chat.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Сервис файлов.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileMetadataRepository repo;
    private final FileMapper mapper;
    private final ApplicationEventPublisher evPub;
    @Value("${chat.file-upload-dir}") private String uploadDir;

    public MessageDto storeAsMessage(Long roomId, Long senderId, MultipartFile file) throws Exception {
        String orig = file.getOriginalFilename();
        String ext = orig.contains(".")? orig.substring(orig.lastIndexOf('.')):"";
        String name = UUID.randomUUID()+ext;
        Path tgt = Paths.get(uploadDir).toAbsolutePath().resolve(name);
        Files.createDirectories(tgt.getParent());
        Files.copy(file.getInputStream(), tgt, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata meta = repo.save(FileMetadata.builder()
                .filename(orig).contentType(file.getContentType())
                .size(file.getSize()).storagePath(tgt.toString())
                .build());
        MessageDto dto = MessageDto.builder()
                .roomId(roomId).senderId(senderId)
                .type("FILE")
                .text(meta.getId().toString())
                .createdAt(Instant.now())
                .build();
        evPub.publishEvent(new FileUploadedEvent(this, dto));
        return dto;
    }

    public Path load(Long id) {
        return Paths.get(repo.findById(id).orElseThrow().getStoragePath());
    }
}
