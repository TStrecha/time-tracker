package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;

public interface UserService {

    /**
     * @param registrationRequest
     * @param role
     * @return
     */
    UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @return
     */
    RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO);

    /**
     * @param relationshipCreateUpdateRequestDTO
     * @param loggedUser
     * @param userContext
     * @return
     */
    RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, LoggedUser loggedUser, UserContext userContext);

    /**
     *
     * @param relationshipCreateUpdateRequestDTO
     * @param loggedUser
     * @param userContext
     * @return
     */
    RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, LoggedUser loggedUser, UserContext userContext);

    /**
     *
     * @param id
     * @param userContext
     * @return
     */
    LoginResponseDTO hasPermissionToChangeContext(Long id, UserContext userContext);

    /**
     * @param loginRequest
     * @return
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest);
}
