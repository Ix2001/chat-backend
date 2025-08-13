package com.company.chat.repository;


import com.company.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Репозиторий комнат.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("select distinct m.room from Membership m where m.user.id = :userId")
    List<Room> findAllByUserId(@Param("userId") Long userId);
}
