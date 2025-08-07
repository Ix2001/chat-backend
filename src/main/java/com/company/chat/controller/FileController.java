package com.company.chat.controller;

import com.company.chat.dto.MessageDto;
import com.company.chat.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST контроллер — файлы.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService svc;

    /** POST /api/files */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageDto upload(@RequestParam("file") MultipartFile file,
                             @RequestParam Long roomId,
                             @RequestParam Long senderId) throws Exception {
        return svc.storeAsMessage(roomId, senderId, file);
    }

    /** GET /api/files/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<UrlResource> download(@PathVariable Long id) throws Exception {
        var path = svc.load(id);
        var res = new UrlResource(path.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }
}
