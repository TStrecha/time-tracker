package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.SettingsDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.service.SettingsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "settings-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "/settings", produces = {APPLICATION_JSON_VALUE})
public class SettingsController {

    private final SettingsService settingsService;

    @PostMapping
    @PreAuthorize("hasPermission(#userContext, 'setings.create')")
    public ResponseEntity<SettingsDTO> createUserSettings(@RequestBody @Valid SettingsDTO settings,
                                                          @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(settingsService.createSettings(settings, userContext), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'settings', 'setings.update')")
    public ResponseEntity<SettingsDTO> updateUserSettings(@PathVariable Long id,
                                                          @RequestBody @Valid SettingsDTO settings,
                                                          @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(settingsService.updateSettings(id, settings, userContext), HttpStatus.OK);
    }
}
