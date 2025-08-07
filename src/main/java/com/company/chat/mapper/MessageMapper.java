package com.company.chat.mapper;

import com.company.chat.dto.MessageDto;
import com.company.chat.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/**
 * MapStruct-маппер Message ↔ MessageDto.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "roomId",   source = "room.id")
    @Mapping(target = "senderId", source = "sender.id")
    MessageDto toDto(Message m);

    @Mapping(target = "room", ignore = true)
    @Mapping(target = "sender", ignore = true)
    Message toEntity(MessageDto dto);
}
