package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.annotation.CustomPermissionCheck;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.PasswordChangeDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.UserUpdateDTO;
import cz.tstrecha.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "user-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "/user", produces = {APPLICATION_JSON_VALUE})
public class UserController {

    private final UserService userService;

    @GetMapping
    @CustomPermissionCheck
    public ResponseEntity<UserContext> loggedUserDetails(@InjectUserContext UserContext user){
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasPermission(#userContext, 'user.update')")
    public ResponseEntity<LoginResponseDTO> changeUserDetails(@RequestBody UserUpdateDTO userUpdateDTO, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.changeUserDetails(userUpdateDTO, userContext), HttpStatus.OK);
    }

    @PostMapping("/relationship")
    @PreAuthorize("hasRole('ACCOUNT_OWNER')")
    public ResponseEntity<RelationshipDTO> createRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                                              @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.createRelationship(relationshipCreateUpdateRequestDTO, userContext), HttpStatus.CREATED);
    }

    @PutMapping("/relationship")
    @PreAuthorize("hasRole('ACCOUNT_OWNER')")
    public ResponseEntity<RelationshipDTO> editRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                                            @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.updateRelationship(relationshipCreateUpdateRequestDTO, userContext), HttpStatus.OK);
    }

    @PutMapping("/context")
    @CustomPermissionCheck
    public ResponseEntity<LoginResponseDTO> changeContext(@RequestParam Long id, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.changeContext(id, userContext), HttpStatus.OK);
    }

    @GetMapping("/context")
    @CustomPermissionCheck
    public ResponseEntity<List<ContextUserDTO>> getAvailableContexts(@InjectUserContext UserContext userContext){
        return new ResponseEntity<>(userService.getActiveRelationships(userContext), HttpStatus.OK);
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('ACCOUNT_OWNER')")
    @CustomPermissionCheck
    public ResponseEntity<LoginResponseDTO> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO,
                                                           @InjectUserContext UserContext userContext) {
        return new ResponseEntity<>(userService.changePassword(passwordChangeDTO, userContext), HttpStatus.OK);
    }
}
