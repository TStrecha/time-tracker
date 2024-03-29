package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper
public interface RelationshipMapper {

    @Mapping(target = "oppositeUserId", source = "to.id")
    @Mapping(target = "displayName", source = "to", qualifiedByName = "mapDisplayName")
    RelationshipDTO toDTOFromReceiving(UserRelationshipEntity userRelationshipEntity);

    @Mapping(target = "oppositeUserId", source = "from.id")
    @Mapping(target = "displayName", source = "from", qualifiedByName = "mapDisplayName")
    RelationshipDTO toDTOFromGiving(UserRelationshipEntity userRelationshipEntity);

    @Mapping(target = "id", ignore = true)
    UserRelationshipEntity fromRequest(RelationshipCreateUpdateRequestDTO request, UserEntity from, UserEntity to);

    @Mapping(target = "from", ignore = true)
    @Mapping(target = "to", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateRelationship(RelationshipCreateUpdateRequestDTO request, @MappingTarget UserRelationshipEntity target);

    @Named("mapDisplayName")
    default String mapDisplayName(UserEntity user){
        return user.getDisplayName();
    }
}
