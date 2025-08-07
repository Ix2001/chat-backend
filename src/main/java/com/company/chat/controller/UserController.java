package com.company.chat.controller;

import com.company.chat.dto.UserDto;
import com.company.chat.dto.PublicKeyDto;
import com.company.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST контроллер — пользователи.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService svc;

    /** GET /api/users */
    @GetMapping
    public List<UserDto> list() {
        return svc.listAll();
    }

    /** GET /api/users/{id}/publicKey */
    @GetMapping("/{id}/publicKey")
    public PublicKeyDto getKey(@PathVariable Long id) {
        return svc.getPublicKey(id);
    }

    /** POST /api/users/{id}/publicKey */
    @PostMapping("/{id}/publicKey")
    public void setKey(@PathVariable Long id, @RequestBody PublicKeyDto dto) {
        svc.setPublicKey(id, dto);
    }
}
