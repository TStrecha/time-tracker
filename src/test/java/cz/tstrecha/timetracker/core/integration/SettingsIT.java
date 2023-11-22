package cz.tstrecha.timetracker.core.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.SettingsCreateUpdateDTO;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.service.SettingsService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingsIT extends IntegrationTest {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @SneakyThrows
    @Transactional
    public void test01_createSetting_success() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var settingDTO = settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        assertNotNull(settingDTO.getValidFrom());
        assertNull(settingDTO.getValidTo());
        assertEquals(BigDecimal.valueOf(100), settingDTO.getMoneyPerHour());
        assertEquals(BigDecimal.valueOf(16000), settingDTO.getMoneyPerMonth());
        assertEquals("TimeTracker", settingDTO.getName());
        assertEquals("Test note", settingDTO.getNote());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test02_createSetting_fail_toIsBeforeFrom() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(LocalDate.now().minusDays(1));
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var exception = assertThrows(UserInputException.class, () -> settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.VALID_FROM_AFTER_VALID_TO, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("Valid from cannot be after valid to.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test03_createSetting_fail_settingWithSameNameAlreadyExists() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));
        var exception = assertThrows(UserInputException.class, () -> settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.SETTING_NAME_NOT_UNIQUE, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("There is already a setting with this name.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test04_createSetting_fail_settingsIntersects() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(LocalDate.now().plusDays(5));
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));
        request.setName("TimeTracker1");
        request.setValidFrom(LocalDate.now().plusDays(4));
        var exception = assertThrows(UserInputException.class, () -> settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.INTERSECTS_WITH_OTHER_SETTINGS, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("There are active settings that would new settings intersect with.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test05_updateSetting_success() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var settingDTO = settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        request.setId(settingDTO.getId());
        request.setValidTo(LocalDate.now().plusMonths(6));
        request.setMoneyPerHour(BigDecimal.valueOf(200));
        request.setMoneyPerMonth(BigDecimal.valueOf(32000));
        request.setName("TimeTrackerUpdated");
        request.setNote("Test note updated");

        var updatedSettingDTO = settingsService.updateSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        assertNotNull(updatedSettingDTO.getValidFrom());
        assertEquals(updatedSettingDTO.getValidFrom().plusMonths(6), updatedSettingDTO.getValidTo());
        assertEquals(BigDecimal.valueOf(200), updatedSettingDTO.getMoneyPerHour());
        assertEquals(BigDecimal.valueOf(32000), updatedSettingDTO.getMoneyPerMonth());
        assertEquals("TimeTrackerUpdated", updatedSettingDTO.getName());
        assertEquals("Test note updated", updatedSettingDTO.getNote());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test06_updateSetting_fail_settingNotFoundById() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(1);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var exception = assertThrows(UserInputException.class, () -> settingsService.updateSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.SETTING_NOT_FOUND_BY_ID, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("Setting not found by id", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test07_updateSetting_fail_updatingNoLongerValidSetting() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now().minusDays(2));
        request.setValidTo(LocalDate.now().minusDays(1));
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var settingDTO = settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        request.setId(settingDTO.getId());
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        var exception = assertThrows(UserInputException.class, () -> settingsService.updateSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.SETTING_NO_LONGER_VALID, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("You cannot change no longer valid settings.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test08_updateSetting_fail_validFromBeforeTo() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now().minusDays(2));
        request.setValidTo(LocalDate.now().minusDays(1));
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var settingDTO = settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        request.setId(settingDTO.getId());
        request.setValidFrom(LocalDate.now().plusDays(1));
        request.setValidTo(LocalDate.now());
        var exception = assertThrows(UserInputException.class, () -> settingsService.updateSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.SETTING_NO_LONGER_VALID, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("You cannot change no longer valid settings.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test09_updateSetting_fail_alreadySettingWithSameName() {
        var user = userRepository.findById(mockUsers(1).get(0)).orElseThrow();

        SettingsCreateUpdateDTO request = new SettingsCreateUpdateDTO();
        request.setId(0);
        request.setValidFrom(LocalDate.now());
        request.setValidTo(null);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName("TimeTracker");
        request.setNote("Test note");

        var settingDTO = settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));
        request.setName("TimeTracker1");
        request.setValidFrom(LocalDate.now().plusMonths(1));
        settingsService.createSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        request.setId(settingDTO.getId());
        request.setName("TimeTracker1");
        var exception = assertThrows(UserInputException.class, () -> settingsService.updateSetting(request, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)));

        assertEquals(ErrorTypeCode.SETTING_NAME_NOT_UNIQUE, exception.getErrorTypeCode());
        assertEquals("SettingsCreateUpdateDTO", exception.getEntityType());
        assertEquals("There is already a setting with this name.", exception.getMessage());
        assertNotNull(exception.getLocalizedMessage());
    }
}
