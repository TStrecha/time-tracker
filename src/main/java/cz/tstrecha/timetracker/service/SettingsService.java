package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.dto.UserContext;

public interface SettingsService {

    /**
     * @param settingsCreateUpdateDTO
     * @param userContext
     * @return
     */
    SettingsCreateUpdateDTO createSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, UserContext userContext);

    /**
     * @param settingsCreateUpdateDTO
     * @param userContext
     * @return
     */
    SettingsCreateUpdateDTO updateSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, UserContext userContext);
}
