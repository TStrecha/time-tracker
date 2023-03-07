package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;

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
