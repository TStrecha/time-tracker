package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectLoggedUser;
import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "user-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "user", produces = {APPLICATION_JSON_VALUE})
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserContext> loggedUserDetails(@InjectUserContext UserContext user){
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/relationship")
    public ResponseEntity<RelationshipDTO> createRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                                              @InjectLoggedUser LoggedUser loggedUser,
                                                              @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.createRelationship(relationshipCreateUpdateRequestDTO, loggedUser, userContext),
                HttpStatus.CREATED);
    }

    @PutMapping("/relationship")
    public ResponseEntity<RelationshipDTO> editRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                 @InjectLoggedUser LoggedUser loggedUser,
                                 @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.updateRelationship(relationshipCreateUpdateRequestDTO,loggedUser,userContext),
                HttpStatus.OK);
    }

    @PostMapping("/context")
    public ResponseEntity<String> hasPermissionToSwitchContext(@RequestParam Long id,
                                                              @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.hasPermissionToChangeContext(id, userContext), HttpStatus.OK);
    }
}
