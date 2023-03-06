package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.*;

public interface UserService {
    /**
     *
     * @param registrationRequest
     * @param role
     * @return
     */
    UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role);

    /**
     *
     * @param request
     * @return
     */
    RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO request);

    /**
     *
     * @param loginRequest
     * @return
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest);
}
