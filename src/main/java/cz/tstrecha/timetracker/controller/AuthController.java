package cz.tstrecha.timetracker.controller;

import cz.tstrecha.timetracker.constant.UserRole;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserDTO;
import cz.tstrecha.timetracker.dto.UserRegistrationRequestDTO;
import cz.tstrecha.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "user-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/time-tracker/v1/auth", produces = {APPLICATION_JSON_VALUE})
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody @Valid UserRegistrationRequestDTO registrationRequest){
        return userService.createUser(registrationRequest, UserRole.USER);
    }

    @PostMapping("/login")
    public LoginResponseDTO loginUser(@RequestBody @Valid LoginRequestDTO loginRequest){
        return userService.loginUser(loginRequest);
    }

}
