package com.company.chat.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.company.chat.dto.CallSessionDto;
import com.company.chat.model.CallSession;

/**
 * MapStruct-маппер CallSession ↔ CallSessionDto.
 */
@Mapper(componentModel = "spring")
public interface CallSessionMapper {

    @Mapping(target = "type",        expression = "java( cs.getType() != null ? cs.getType().name() : null )")
    @Mapping(target = "status",      expression = "java( cs.getStatus() != null ? cs.getStatus().name() : null )")
    @Mapping(target = "initiatorId", source = "initiator.id")
    @Mapping(target = "receiverId",  source = "receiver.id")
    CallSessionDto toDto(CallSession cs);

    @Mapping(target = "type",      expression = "java( dto.getType() != null ? com.company.chat.model.CallType.valueOf(dto.getType()) : null )")
    @Mapping(target = "status",    expression = "java( dto.getStatus() != null ? com.company.chat.model.CallStatus.valueOf(dto.getStatus()) : null )")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "receiver",  ignore = true)
    CallSession toEntity(CallSessionDto dto);
}
