package com.company.chat.service;

import com.company.chat.controller.AuthController;
import com.company.chat.dto.PublicKeyDto;
import com.company.chat.dto.UserDto;
import com.company.chat.mapper.UserMapper;
import com.company.chat.model.User;
import com.company.chat.repository.UserRepository;
import com.company.chat.security.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import static java.util.stream.Collectors.*;

/**
 * Сервис пользователей.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;

    public List<UserDto> listAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(toList());
    }

    public PublicKeyDto getPublicKey(Long userId) {
        return mapper.toPublicKeyDto(repo.findById(userId).orElseThrow());
    }

    public void setPublicKey(Long userId, PublicKeyDto dto) {
        var u = repo.findById(userId).orElseThrow();
        mapper.updatePublicKey(u, dto);
        repo.save(u);
    }
    public void register(RegisterRequest req) {
        User u = User.builder()
                .username(req.getUsername())
                .displayName(req.getDisplayName())
                .password(encoder.encode(req.getPassword()))
                .roles("USER")
                .build();
        repo.save(u);
    }
}
