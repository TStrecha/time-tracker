package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.CustomPermissionCheck;
import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.service.RelationshipService;
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
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "/relationship", produces = {APPLICATION_JSON_VALUE})
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping
    @PreAuthorize("hasRole('ACCOUNT_OWNER')")
    public ResponseEntity<RelationshipDTO> createRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                                              @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(relationshipService.createRelationship(relationshipCreateUpdateRequestDTO, userContext), HttpStatus.CREATED);
    }

    @PutMapping
    @PreAuthorize("hasPermission(#relationshipCreateUpdateRequestDTO.id, 'relationship', 'relationship.update') && hasRole('ACCOUNT_OWNER')")
    public ResponseEntity<RelationshipDTO> editRelationship(@RequestBody RelationshipCreateUpdateRequestDTO relationshipCreateUpdateRequestDTO,
                                                            @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(relationshipService.updateRelationship(relationshipCreateUpdateRequestDTO, userContext), HttpStatus.OK);
    }
    @GetMapping("/context")
    @CustomPermissionCheck
    public ResponseEntity<List<ContextUserDTO>> getAvailableContexts(@InjectUserContext UserContext userContext){
        return new ResponseEntity<>(relationshipService.getActiveContexts(userContext), HttpStatus.OK);
    }

    @PutMapping("/context")
    @CustomPermissionCheck
    public ResponseEntity<LoginResponseDTO> changeContext(@RequestParam Long id, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(relationshipService.changeContext(id, userContext), HttpStatus.OK);
    }

}
