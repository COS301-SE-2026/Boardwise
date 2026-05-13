package com.boardwise.backend.vault.mapper;

import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.model.Rulebook;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RulebookMapper {
    // Map ObjectId to String using a custom helper
    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "contributorId", source = "contributorId", qualifiedByName = "objectIdToString")
    // 'lockHeldBy' exists in DTO but not in Model, so it is ignored during auto-mapping
    @Mapping(target = "lockHeldBy", ignore = true)
    RulebookResponseDto toDto(Rulebook rulebook);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return (objectId != null) ? objectId.toHexString() : null;
    }
}