package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoggedUser;
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

    protected RelationshipMapper relationshipMapper = Mappers.getMapper(RelationshipMapper.class);

    public abstract UserEntity fromRegistrationRequest(UserRegistrationRequestDTO registrationRequest, String passwordHashed, UserRole role);

    @Mapping(target = "relationsReceiving", source = "user", qualifiedByName = "mapRelationsReceiving")
    @Mapping(target = "relationsGiving", source = "user", qualifiedByName = "mapRelationsGiving")
    @Mapping(target = "displayName", source = "user", qualifiedByName = "mapDisplayName")
    public abstract UserDTO toDTO(UserEntity user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "relationshipsReceiving", source = "user.activeRelationshipsReceiving")
    @Mapping(target = "fullName", source = "user", qualifiedByName = "mapDisplayName")
    @Mapping(target = "activePermissions", expression = "java(mapActivePermissions(user, loggedAs))")
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "activeRelationshipsReceiving", ignore = true)
    public abstract UserContext toContext(UserEntity user, ContextUserDTO loggedAs);

    @Mapping(target = "id", source = "from.id")
    @Mapping(target = "fullName", source = "from", qualifiedByName = "mapDisplayName")
    @Mapping(target = "email", source = "from.email")
    @Mapping(target = "accountType", source = "from.accountType")
    public abstract ContextUserDTO userRelationshipEntityToContextUserDTO(UserRelationshipEntity relationship);

    @Mapping(target = "id", source = "contextUser.id")
    @Mapping(target = "email", source = "contextUser.email")
    @Mapping(target = "accountType", source = "contextUser.accountType")
    public abstract LoggedUser toLoggedUser(ContextUserDTO contextUser, UserEntity userEntity);

    public abstract void updateUser(UserUpdateDTO userUpdateDTO, @MappingTarget UserEntity user);

    @Named("mapDisplayName")
    protected String mapDisplayName(UserEntity user){
        return user.getAccountType() == AccountType.PERSON ? user.getFirstName() + " " + user.getLastName() : user.getCompanyName();
    }

    @Named("mapRelationsReceiving")
    protected List<RelationshipDTO> mapRelationsReceiving(UserEntity user){
        return user.getUserRelationshipReceiving().stream().map(relation -> relationshipMapper.toDTOFromReceiving(relation)).toList();
    }

    @Named("mapRelationsGiving")
    protected List<RelationshipDTO> mapRelationsGiving(UserEntity user){
        return user.getUserRelationshipGiving().stream().map(relation -> relationshipMapper.toDTOFromGiving(relation)).toList();
    }

    protected List<String> mapActivePermissions(UserEntity user, ContextUserDTO loggedAs){
        return user.getUserRelationshipReceiving().stream()
                .filter(r -> r.getTo().getId().equals(loggedAs.getId()))
                .findFirst()
                .map(UserRelationshipEntity::getPermissions)
                .orElse(List.of());
    }
}
