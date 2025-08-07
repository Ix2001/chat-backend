package com.company.chat.service;

import com.company.chat.dto.CallSessionDto;
import com.company.chat.event.CallEvent;
import com.company.chat.mapper.CallSessionMapper;
import com.company.chat.model.*;
import com.company.chat.repository.CallSessionRepository;
import com.company.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис звонков.
 */
@Service
@RequiredArgsConstructor
public class CallService {
    private final CallSessionRepository repo;
    private final UserRepository userRepo;
    private final CallSessionMapper mapper;
    private final ApplicationEventPublisher evPub;

    public CallSessionDto initiate(String callId, CallType type, Long from, Long to) {
        User f = userRepo.findById(from).orElseThrow();
        User rcv = userRepo.findById(to).orElseThrow();
        CallSession cs = CallSession.builder()
                .callId(callId).type(type)
                .initiator(f).receiver(rcv)
                .status(CallStatus.INITIATED)
                .startTime(Instant.now()).build();
        cs = repo.save(cs);
        CallSessionDto dto = mapper.toDto(cs);
        evPub.publishEvent(new CallEvent(this, dto,
                String.format("{\"action\":\"callOffer\",\"callId\":\"%s\",\"from\":%d,\"to\":%d}", callId, from, to)
        ));
        return dto;
    }

    public CallSessionDto ring(String callId) {
        var cs = repo.findByCallId(callId).orElseThrow();
        cs.setStatus(CallStatus.RINGING);
        cs = repo.save(cs);
        CallSessionDto dto = mapper.toDto(cs);
        evPub.publishEvent(new CallEvent(this, dto,
                String.format("{\"action\":\"callRing\",\"callId\":\"%s\",\"from\":%d,\"to\":%d}",
                        callId, cs.getReceiver().getId(), cs.getInitiator().getId())
        ));
        return dto;
    }

    public CallSessionDto answer(String callId) {
        var cs = repo.findByCallId(callId).orElseThrow();
        cs.setStatus(CallStatus.IN_PROGRESS); cs.setAnswerTime(Instant.now());
        cs = repo.save(cs);
        CallSessionDto dto = mapper.toDto(cs);
        evPub.publishEvent(new CallEvent(this, dto,
                String.format("{\"action\":\"callAnswer\",\"callId\":\"%s\",\"from\":%d,\"to\":%d}",
                        callId, cs.getReceiver().getId(), cs.getInitiator().getId())
        ));
        return dto;
    }

    public CallSessionDto reject(String callId) {
        var cs = repo.findByCallId(callId).orElseThrow();
        cs.setStatus(CallStatus.REJECTED); cs.setEndTime(Instant.now());
        cs = repo.save(cs);
        CallSessionDto dto = mapper.toDto(cs);
        evPub.publishEvent(new CallEvent(this, dto,
                String.format("{\"action\":\"callReject\",\"callId\":\"%s\",\"from\":%d,\"to\":%d}",
                        callId, cs.getInitiator().getId(), cs.getReceiver().getId())
        ));
        return dto;
    }

    public CallSessionDto end(String callId) {
        var cs = repo.findByCallId(callId).orElseThrow();
        cs.setStatus(CallStatus.ENDED); cs.setEndTime(Instant.now());
        cs = repo.save(cs);
        CallSessionDto dto = mapper.toDto(cs);
        evPub.publishEvent(new CallEvent(this, dto,
                String.format("{\"action\":\"callEnd\",\"callId\":\"%s\",\"from\":%d,\"to\":%d}",
                        callId, cs.getInitiator().getId(), cs.getReceiver().getId())
        ));
        return dto;
    }

    public List<CallSessionDto> listAll() {
        var cs = repo.findAll();
        return cs.stream().map(mapper::toDto).toList();
    }
}
