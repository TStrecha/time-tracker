package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "user-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "auth", produces = {APPLICATION_JSON_VALUE})
public class AuthController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<Long> registerUser(@RequestBody @Valid UserRegistrationRequestDTO registrationRequest){
        return new ResponseEntity<>(userService.createUser(registrationRequest, UserRole.USER).getId(), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody @Valid LoginRequestDTO loginRequest){
        return new ResponseEntity<>(userService.loginUser(loginRequest), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody String token){
        return new ResponseEntity<>(authenticationService.refreshToken(token), HttpStatus.OK);
    }
}
