package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectLoggedUser;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.service.SettingsService;
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
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "settings", produces = {APPLICATION_JSON_VALUE})
public class SettingsController {

    private final SettingsService settingsService;

    @PostMapping
    public ResponseEntity<SettingsCreateUpdateDTO> createUserSetting(@RequestBody @Valid SettingsCreateUpdateDTO setting,
                                                                     @InjectLoggedUser LoggedUser loggedUser){
        return new ResponseEntity<>(settingsService.createSetting(setting, loggedUser), HttpStatus.OK);
    }
}
