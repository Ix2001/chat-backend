package com.company.chat.service;

import com.company.chat.dto.PublicKeyDto;
import com.company.chat.dto.UserDto;
import com.company.chat.mapper.UserMapper;
import com.company.chat.model.User;
import com.company.chat.repository.UserRepository;
import com.company.chat.security.TokenUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public User ensureFromToken(TokenUser tu) {
        return repo.findByExternalId(tu.externalId())
                .map(u -> {
                    boolean changed = false;
                    if (tu.username()!=null && !tu.username().equals(u.getUsername())) { u.setUsername(tu.username()); changed = true; }
                    if (tu.displayName()!=null && !tu.displayName().equals(u.getDisplayName())) { u.setDisplayName(tu.displayName()); changed = true; }
                    if (tu.email()!=null && !tu.email().equals(u.getEmail())) { u.setEmail(tu.email()); changed = true; }
                    return changed ? repo.save(u) : u;
                })
                .orElseGet(() -> repo.save(User.builder()
                        .externalId(tu.externalId())
                        .username(tu.username())
                        .displayName(tu.displayName())
                        .email(tu.email())
                        .build()));
    }
}
