package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.UserContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "user-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "user", produces = {APPLICATION_JSON_VALUE})
public class UserController {

    @GetMapping
    public ResponseEntity<UserContext> loggedUserDetails(@InjectUserContext UserContext user){
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
