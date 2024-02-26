package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.PasswordChangeDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.UserUpdateDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;

import java.util.List;

public interface UserService {

    /**
     * @param registrationRequest
     * @param role
     * @return
     */
    UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @param userContext
     * @return
     */
    RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, UserContext userContext);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @param userContext
     * @return
     */
    RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, UserContext userContext);

    /**
     * @param id
     * @param userContext
     * @return
     */
    LoginResponseDTO changeContext(Long id, UserContext userContext);

    /**
     *
     * @param userUpdateDTO
     * @param userContext
     * @return
     */
    LoginResponseDTO changeUserDetails(UserUpdateDTO userUpdateDTO, UserContext userContext);

    /**
     * @param loginRequest
     * @return
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest);

    /**
     *
     * @param passwordChangeDTO
     * @param userContext
     * @return
     */
    LoginResponseDTO changePassword(PasswordChangeDTO passwordChangeDTO, UserContext userContext);

    /**
     * @param userContext
     * @return
     */
    List<ContextUserDTO> getActiveRelationships(UserContext userContext);

    /**
     * @param userContext
     * @return
     */
    UserEntity getUserFromContext(UserContext userContext);

    /**
     *
     * @param userContext
     * @return
     */
    UserEntity getLoggedUserFromContext(UserContext userContext);
}
