package com.company.chat.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.company.chat.dto.RoomDto;
import com.company.chat.model.Room;

/**
 * MapStruct-маппер Room ↔ RoomDto.
 */
@Mapper(componentModel = "spring")
public interface RoomMapper {
    @Mapping(target = "type", expression = "java( r.getType() != null ? r.getType().name() : null )")
    RoomDto toDto(Room r);

    @Mapping(target = "type", expression = "java( dto.getType() != null ? com.company.chat.model.RoomType.valueOf(dto.getType()) : null )")
    Room toEntity(RoomDto dto);
}
