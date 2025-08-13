package com.company.chat.mapper;

import com.company.chat.config.MapStructConfig;
import com.company.chat.dto.PublicKeyDto;
import com.company.chat.dto.UserDto;
import com.company.chat.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;


/**
 * MapStruct-маппер User ↔ UserDto/PublicKeyDto.
 */
@Mapper(config = MapStructConfig.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserDto toDto(User u);
    User toEntity(UserDto dto);

    void updatePublicKey(@MappingTarget User user, PublicKeyDto dto);

    PublicKeyDto toPublicKeyDto(User u);
}