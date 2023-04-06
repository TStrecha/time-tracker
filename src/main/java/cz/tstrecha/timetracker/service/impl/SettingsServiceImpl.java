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

import java.time.LocalDate;

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

        if (userSettingsRepository.existsByUserAndNameAndIdIsNot(user.getUserEntity(), settingsCreateUpdateDTO.getName(), settingsCreateUpdateDTO.getId())){
            throw new UserInputException("There is already a setting with this name.", ErrorTypeCode.SETTINGS_NAME_NOT_UNIQUE, "SettingsCreateUpdateDTO");
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

    @Override
    @Transactional
    public SettingsCreateUpdateDTO updateSetting(SettingsCreateUpdateDTO settingsCreateUpdateDTO, LoggedUser user) {
        var setting = userSettingsRepository.findByIdAndUser(settingsCreateUpdateDTO.getId(), user.getUserEntity())
                .orElseThrow(() -> new UserInputException("Setting not found by id", ErrorTypeCode.SETTING_NOT_FOUND_BY_ID, "SettingsCreateUpdateDTO"));

        if (setting.getValidTo() != null && setting.getValidTo().isBefore(LocalDate.now())){
            throw new UserInputException("You cannot change no longer valid settings.", ErrorTypeCode.SETTING_NO_LONGER_VALID, "SettingsCreateUpdateDTO");
        }

        if (settingsCreateUpdateDTO.getValidTo() != null && settingsCreateUpdateDTO.getValidTo().isBefore(setting.getValidFrom())){
            throw new UserInputException("Valid from cannot be after valid to.", ErrorTypeCode.VALID_FROM_AFTER_VALID_TO, "SettingsCreateUpdateDTO");
        }

        if (userSettingsRepository.existsByUserAndNameAndIdIsNot(user.getUserEntity(), settingsCreateUpdateDTO.getName(), settingsCreateUpdateDTO.getId())){
            throw new UserInputException("There is already a setting with this name.", ErrorTypeCode.SETTINGS_NAME_NOT_UNIQUE, "SettingsCreateUpdateDTO");
        }

        settingsMapper.updateSetting(settingsCreateUpdateDTO, setting);

        userSettingsRepository.save(setting);

        return settingsMapper.toDTO(setting);
    }
}
