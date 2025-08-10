package com.company.chat.repository;

import com.company.chat.model.Membership;
import com.company.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Репозиторий участий комнаты.
 */
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    List<Membership> findByRoom(Room room);
    @Query("select m.user.id from Membership m where m.room.id = :roomId")
    List<Long> findUserIdsByRoomId(@Param("roomId") Long roomId);

}
