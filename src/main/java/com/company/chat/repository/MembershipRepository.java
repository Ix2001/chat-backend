package com.company.chat.repository;

import com.company.chat.model.Membership;
import com.company.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий участий комнаты.
 */
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByRoom(Room room);
}
