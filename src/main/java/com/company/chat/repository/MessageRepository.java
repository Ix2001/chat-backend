package com.company.chat.repository;

import com.company.chat.model.Message;
import com.company.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий сообщений.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomOrderByTimestamp(Room room);
}
