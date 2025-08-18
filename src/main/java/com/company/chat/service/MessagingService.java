package com.company.chat.service;

import com.company.chat.dto.MessageDto;
import com.company.chat.event.MessageSentEvent;
import com.company.chat.mapper.MessageMapper;
import com.company.chat.model.Message;
import com.company.chat.model.Room;
import com.company.chat.model.User;
import com.company.chat.repository.MessageRepository;
import com.company.chat.repository.RoomRepository;
import com.company.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import static java.util.stream.Collectors.*;

/**
 * Сервис сообщений.
 */
@Service
@RequiredArgsConstructor
public class MessagingService {
    private final MessageRepository msgRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    private final MessageMapper mapper;
    private final ApplicationEventPublisher evPub;

    public MessageDto saveEncrypted(Long roomId, Long senderId, String content) {
        Room r = roomRepo.findById(roomId).orElseThrow();
        User u = userRepo.findById(senderId).orElseThrow();
        Message m = Message.builder()
                .room(r).sender(u)
                .type("ENCRYPTED").content(content)
                .timestamp(Instant.now()).build();
        m = msgRepo.save(m);
        var dto = mapper.toDto(m);
        evPub.publishEvent(new MessageSentEvent(this, dto));
        return dto;
    }

    public Message saveEncryptedEntity(Long roomId, Long senderId, String content) {
        Room r = roomRepo.findById(roomId).orElseThrow();
        User u = userRepo.findById(senderId).orElseThrow();
        Message m = Message.builder()
                .room(r).sender(u)
                .type("ENCRYPTED").content(content)
                .timestamp(Instant.now()).build();
        return msgRepo.save(m);
    }

    public List<MessageDto> history(Long roomId) {
        Room r = roomRepo.findById(roomId).orElseThrow();
        return msgRepo.findByRoomOrderByTimestamp(r).stream()
                .map(mapper::toDto)
                .collect(toList());
    }
}
