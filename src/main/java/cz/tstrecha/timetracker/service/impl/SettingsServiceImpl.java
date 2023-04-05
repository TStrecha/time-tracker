package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.dto.mapper.SettingsMapper;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.service.SettingsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final UserSettingsRepository userSettingsRepository;

    private final SettingsMapper settingsMapper;

    @Override
    @Transactional
    public SettingsCreateUpdateDTO createSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, LoggedUser user){
        if (settingsCreateUpdateDTO.getValidTo() != null && settingsCreateUpdateDTO.getValidFrom().isAfter(settingsCreateUpdateDTO.getValidTo())){
            throw new UserInputException("Valid from cannot be after valid to.", ErrorTypeCode.VALID_FROM_AFTER_VALID_TO, "SettingsCreateUpdateDTO");
        }

        userSettingsRepository.findActiveUserSettings(user.getUserEntity())
            .forEach(setting -> {
                if (setting.getValidTo() == null){
                    setting.setValidTo(settingsCreateUpdateDTO.getValidFrom().minusDays(1));
                    userSettingsRepository.save(setting);
                } else if (setting.getValidTo().isAfter(settingsCreateUpdateDTO.getValidFrom())) {
                    throw new UserInputException("There are active settings that would new settings intersect with.", ErrorTypeCode.INTERSECTS_WITH_OTHER_SETTINGS, "SettingsCreateUpdateDTO");
                }
            });

        var newSetting = settingsMapper.toEntity(settingsCreateUpdateDTO, user.getUserEntity());
        newSetting = userSettingsRepository.save(newSetting);

        return settingsMapper.toDTO(newSetting);
    }
}
