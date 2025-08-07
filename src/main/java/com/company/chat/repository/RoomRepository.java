package com.company.chat.repository;


import com.company.chat.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий комнат.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {}
