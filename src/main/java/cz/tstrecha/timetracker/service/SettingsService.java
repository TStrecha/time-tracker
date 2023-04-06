package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;

public interface SettingsService {

    /**
     *
     * @param settingsCreateUpdateDTO
     * @param user
     * @return
     */
    SettingsCreateUpdateDTO createSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, LoggedUser user);

    /**
     *
     * @param settingsCreateUpdateDTO
     * @param user
     * @return
     */
    SettingsCreateUpdateDTO updateSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, LoggedUser user);
}
