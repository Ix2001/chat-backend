package com.company.chat.service;

import com.company.chat.dto.RoomDto;
import com.company.chat.mapper.RoomMapper;
import com.company.chat.model.Membership;
import com.company.chat.model.Room;
import com.company.chat.model.RoomType;
import com.company.chat.model.User;
import com.company.chat.repository.MembershipRepository;
import com.company.chat.repository.RoomRepository;
import com.company.chat.repository.UserRepository;
import com.company.chat.websocket.ChatWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.*;

/**
 * Сервис комнат.
 */
@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepo;
    private final MembershipRepository memRepo;
    private final UserRepository userRepo;
    private final RoomMapper mapper;
    private final WebSocketPushService wsHandler;
    private final ObjectMapper objectMapper;

    public RoomDto createDirect(Long u1, Long u2) {
        User a = userRepo.findById(u1).orElseThrow();
        User b = userRepo.findById(u2).orElseThrow();
        Room r = Room.builder().type(RoomType.DIRECT).build();
        r = roomRepo.save(r);
        memRepo.save(new Membership(null,a,r));
        memRepo.save(new Membership(null,b,r));
        return mapper.toDto(r);
    }

    public RoomDto createGroup(String name, List<Long> users) {
        Room room = Room.builder().type(RoomType.GROUP).name(name).build();
        final Room savedRoom = roomRepo.save(room);
        users.forEach(id -> memRepo.save(new Membership(null,userRepo.findById(id).orElseThrow(), savedRoom)));
        return mapper.toDto(savedRoom);
    }

    public List<RoomDto> listAll() {
        return roomRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(toList());
    }

    public List<Long> getMemberIds(Long roomId) {
        var r = roomRepo.findById(roomId).orElseThrow();
        return memRepo.findByRoom(r).stream()
                .map(m -> m.getUser().getId())
                .collect(toList());
    }

    /**
     * Пригласить пользователей в существующую комнату:
     * 1) добавить в БД memberships,
     * 2) отправить каждому WS-сообщение {action: "invite", roomId, roomName}.
     */
    @Transactional
    public void inviteUsers(Long roomId, List<Long> userIds) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        // 1) Сохраняем в БД
        userIds.forEach(uid -> {
            User u = userRepo.findById(uid)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + uid));
            memRepo.save(Membership.builder().user(u).room(room).build());
        });

        // 2) Рассылаем уведомление через WebSocket
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("action", "invite");
        msg.put("roomId", room.getId());
        msg.put("roomName", room.getName() != null ? room.getName() : "");
        TextMessage wsMessage = new TextMessage(msg.toString());

        TextMessage wsMessage1 = new TextMessage(msg.toString());
        userIds.forEach(uid -> wsHandler.sendTo(uid, wsMessage1));
    }

    @Transactional
    public List<RoomDto> getByUser(Long userId) {
        return roomRepo.findById(userId).stream().map(mapper::toDto).toList();
    }
}
