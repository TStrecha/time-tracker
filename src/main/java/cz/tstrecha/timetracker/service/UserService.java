package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.PasswordChangeDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.dto.UserUpdateDTO;

public interface UserService {

    /**
     * @param registrationRequest
     * @param role
     * @return
     */
    UserDTO createUser(UserRegistrationRequestDTO registrationRequest, UserRole role);

    /**
     * @param loginRequest
     * @return
     */
    LoginResponseDTO loginUser(LoginRequestDTO loginRequest);

    /**
     *
     * @param userUpdateDTO
     * @param userContext
     * @return
     */
    LoginResponseDTO changeUserDetails(UserUpdateDTO userUpdateDTO, UserContext userContext);

    /**
     *
     * @param passwordChangeDTO
     * @param userContext
     * @return
     */
    LoginResponseDTO changePassword(PasswordChangeDTO passwordChangeDTO, UserContext userContext);
}
