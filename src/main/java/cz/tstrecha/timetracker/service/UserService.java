package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.*;

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
    RelationshipDTO updateRelationShip(RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO, LoggedUser loggedUser, UserContext userContext);

    /**
     * @param loginRequest
     * @return
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest);
}
