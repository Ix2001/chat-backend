package com.company.chat.mapper;

import com.company.chat.config.MapStructConfig;
import com.company.chat.dto.MessageDto;
import com.company.chat.model.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


/**
 * MapStruct-маппер Message ↔ MessageDto.
 */
@Mapper(config = MapStructConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {
    @Mapping(target = "roomId", source = "room.id")
    MessageDto toDto(Message m);

    @Mapping(target = "room", ignore = true)
    @Mapping(target = "sender", ignore = true)
    Message toEntity(MessageDto dto);
}
