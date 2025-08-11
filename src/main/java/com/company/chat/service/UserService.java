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

    @Transactional
    public User ensureFromToken(TokenUser tu) {
        var u = repo.findByUsername(tu.username())
                .orElseGet(() -> User.builder()
                        .username(tu.username())
                        .displayName(tu.displayName())
                        .build());

        boolean changed = false;

        if (u.getId() == null) {
            return repo.save(u);
        }
        if (!equalsNullable(u.getDisplayName(), tu.displayName())) {
            u.setDisplayName(tu.displayName());
            changed = true;
        }
        return changed ? repo.save(u) : u;
    }

    private static boolean equalsNullable(String a, String b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    public PublicKeyDto getPublicKey(Long userId) {
        return mapper.toPublicKeyDto(repo.findById(userId).orElseThrow());
    }

    public void setPublicKey(Long userId, PublicKeyDto dto) {
        var u = repo.findById(userId).orElseThrow();
        mapper.updatePublicKey(u, dto);
        repo.save(u);
    }
}
