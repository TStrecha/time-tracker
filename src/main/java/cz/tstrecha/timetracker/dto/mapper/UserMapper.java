package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.UserUpdateDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.repository.entity.UserRelationshipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public abstract class UserMapper {

    protected final RelationshipMapper relationshipMapper = Mappers.getMapper(RelationshipMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "secretMode", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "userRelationshipGiving", ignore = true)
    @Mapping(target = "userRelationshipReceiving", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "activeRelationshipsReceiving", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    public abstract UserEntity fromRegistrationRequest(UserRegistrationRequestDTO registrationRequest, String passwordHashed, UserRole role);

    @Mapping(target = "relationsReceiving", source = "user", qualifiedByName = "mapRelationsReceiving")
    @Mapping(target = "relationsGiving", source = "user", qualifiedByName = "mapRelationsGiving")
    @Mapping(target = "displayName", source = "user", qualifiedByName = "mapDisplayName")
    public abstract UserDTO toDTO(UserEntity user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = "user", qualifiedByName = "mapDisplayName")
    @Mapping(target = "activePermissions", expression = "java(mapActivePermissions(user, loggedAs))")
    @Mapping(target = "authorities", ignore = true)
    public abstract UserContext toContext(UserEntity user, ContextUserDTO loggedAs);

    @Mapping(target = "id", source = "from.id")
    @Mapping(target = "fullName", source = "from", qualifiedByName = "mapDisplayName")
    @Mapping(target = "email", source = "from.email")
    @Mapping(target = "accountType", source = "from.accountType")
    public abstract ContextUserDTO userRelationshipEntityToContextUserDTO(UserRelationshipEntity relationship);

    @Mapping(target = "activeFrom", ignore = true)
    @Mapping(target = "activeTo", ignore = true)
    @Mapping(target = "fullName", source = "user.displayName")
    @Mapping(target = "secureValues", constant = "false")
    public abstract ContextUserDTO userToContextUserDTO(UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHashed", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "userRelationshipGiving", ignore = true)
    @Mapping(target = "userRelationshipReceiving", ignore = true)
    @Mapping(target = "settings", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "activeRelationshipsReceiving", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    public abstract void updateUser(UserUpdateDTO userUpdateDTO, @MappingTarget UserEntity user);

    @Named("mapDisplayName")
    protected String mapDisplayName(UserEntity user){
        return user.getDisplayName();
    }

    @Named("mapRelationsReceiving")
    protected List<RelationshipDTO> mapRelationsReceiving(UserEntity user){
        return user.getUserRelationshipReceiving().stream().map(relationshipMapper::toDTOFromReceiving).toList();
    }

    @Named("mapRelationsGiving")
    protected List<RelationshipDTO> mapRelationsGiving(UserEntity user){
        return user.getUserRelationshipGiving().stream().map(relationshipMapper::toDTOFromGiving).toList();
    }

    protected List<String> mapActivePermissions(UserEntity user, ContextUserDTO loggedAs){
        return user.getUserRelationshipReceiving().stream()
                .filter(r -> r.getTo().getId().equals(loggedAs.getId()))
                .findFirst()
                .map(UserRelationshipEntity::getPermissions)
                .orElse(List.of());
    }
}
