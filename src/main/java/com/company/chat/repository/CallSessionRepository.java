package com.company.chat.repository;

import com.company.chat.model.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Репозиторий сессий звонков.
 */
public interface CallSessionRepository extends JpaRepository<CallSession, Long> {
    Optional<CallSession> findByCallId(String callId);
}
