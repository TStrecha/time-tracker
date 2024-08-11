package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.SettingsDTO;
import cz.tstrecha.timetracker.dto.UserContext;

public interface SettingsService {

    /**
     * @param settingsRequest
     * @param userContext
     * @return
     */
    SettingsDTO createSettings(SettingsDTO settingsRequest, UserContext userContext);

    /**
     * @param id
     * @param settingsRequest
     * @param userContext
     * @return
     */
    SettingsDTO updateSettings(Long id, SettingsDTO settingsRequest, UserContext userContext);
}
