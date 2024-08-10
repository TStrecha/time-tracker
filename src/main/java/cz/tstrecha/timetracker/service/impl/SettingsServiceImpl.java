package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.SettingsDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.SettingsMapper;
import cz.tstrecha.timetracker.repository.UserSettingsRepository;
import cz.tstrecha.timetracker.service.SettingsService;
import cz.tstrecha.timetracker.service.UserRetrievalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final UserSettingsRepository userSettingsRepository;

    private final UserRetrievalService userRetrievalService;

    private final SettingsMapper settingsMapper;

    @Override
    @Transactional
    public SettingsDTO createSettings(SettingsDTO settingsRequest, UserContext userContext){
        var user = userRetrievalService.getUserFromContext(userContext);

        if (settingsRequest.getValidTo() != null && settingsRequest.getValidFrom().isAfter(settingsRequest.getValidTo())){
            throw new UserInputException(
                    "Valid from cannot be after valid to.",
                    ErrorTypeCode.VALID_FROM_AFTER_VALID_TO,
                    SettingsDTO.class);
        }

        if (userSettingsRepository.existsByUserAndName(user, settingsRequest.getName())){
            throw new UserInputException(
                    "There is already a setting with this name.",
                    ErrorTypeCode.SETTING_NAME_NOT_UNIQUE,
                    SettingsDTO.class);
        }

        userSettingsRepository.findActiveUserSettings(user)
            .forEach(setting -> {
                if (setting.getValidTo() == null){
                    setting.setValidTo(settingsRequest.getValidFrom().minusDays(1));
                    userSettingsRepository.save(setting);
                } else if (setting.getValidTo().isAfter(settingsRequest.getValidFrom())) {
                    throw new UserInputException(
                            "There are active settings that would new settings intersect with.",
                            ErrorTypeCode.INTERSECTS_WITH_OTHER_SETTINGS,
                            SettingsDTO.class);
                }
            });

        var newSetting = settingsMapper.toEntity(settingsRequest, user);
        newSetting = userSettingsRepository.save(newSetting);

        return settingsMapper.toDTO(newSetting);
    }

    @Override
    @Transactional
    public SettingsDTO updateSettings(Long id, SettingsDTO settingsRequest, UserContext userContext) {
        var setting = userSettingsRepository.findById(id)
                .orElseThrow(() -> new UserInputException(
                        "Setting not found by id",
                        ErrorTypeCode.SETTING_NOT_FOUND_BY_ID,
                        SettingsDTO.class));

        if (setting.getValidTo() != null && setting.getValidTo().isBefore(LocalDate.now())){
            throw new UserInputException(
                    "You cannot change no longer valid settings.",
                    ErrorTypeCode.SETTING_NO_LONGER_VALID,
                    SettingsDTO.class);
        }

        if (settingsRequest.getValidTo() != null && settingsRequest.getValidTo().isBefore(settingsRequest.getValidFrom())){
            throw new UserInputException(
                    "Valid from cannot be after valid to.",
                    ErrorTypeCode.VALID_FROM_AFTER_VALID_TO,
                    SettingsDTO.class);
        }

        if (userSettingsRepository.existsByUserIdAndNameAndIdIsNot(userContext.getCurrentUserId(), settingsRequest.getName(), id)){
            throw new UserInputException(
                    "There is already a setting with this name.",
                    ErrorTypeCode.SETTING_NAME_NOT_UNIQUE,
                    SettingsDTO.class);
        }

        settingsMapper.updateSetting(settingsRequest, setting);
        userSettingsRepository.save(setting);

        return settingsMapper.toDTO(setting);
    }
}
