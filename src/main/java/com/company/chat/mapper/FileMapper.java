package com.company.chat.mapper;

import com.company.chat.dto.FileDto;
import com.company.chat.model.FileMetadata;
import org.mapstruct.Mapper;


/**
 * MapStruct-маппер FileMetadata → FileDto.
 */
@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDto toDto(FileMetadata f);
}
