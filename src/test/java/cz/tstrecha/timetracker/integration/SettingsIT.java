package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.dto.SettingsDTO;
import cz.tstrecha.timetracker.utils.ResultActionsHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static cz.tstrecha.timetracker.utils.RequestBuilder.buildRequest;
import static cz.tstrecha.timetracker.utils.assertions.UserInputExceptionHandler.handleUserInputException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettingsIT extends IntegrationTest {

    private final static String SETTINGS_API_BASE_PATH = "/settings";

    @Test
    @SneakyThrows
    void should_CreateSettings_When_ValidRequestGiven() {
        var request = buildSettingsRequest();
        var settings = createSettings(request);

        assertSettings(request, settings);
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_ValidToIsBeforeValidFrom() {
        var request = buildSettingsRequest(LocalDate.now().minusDays(5));
        sendCreateRequest(request)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.VALID_FROM_AFTER_VALID_TO));
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_SettingsWithSameNameExistsForSameUser() {
        var request = buildSettingsRequest();
        createSettings(request);

        sendCreateRequest(request)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.SETTING_NAME_NOT_UNIQUE));
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_NewSettingsWouldIntersectWithExistingOnes() {
        var request = buildSettingsRequest(LocalDate.now().plusDays(5));
        sendCreateRequest(request);

        var secondRequest = buildSettingsRequest(null, "TimeTracker2", LocalDate.now().plusDays(1));
        sendCreateRequest(secondRequest)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.INTERSECTS_WITH_OTHER_SETTINGS));
    }

    @Test
    @SneakyThrows
    void should_BeSuccessfullyUpdateSettings_When_ValidRequestGiven() {
        var request = buildSettingsRequest();
        var settings = createSettings(request);

        var updateRequest = buildSettingsRequest(settings.getId(), "TimeTrackerUpdated", LocalDate.now().plusMonths(6));
        updateRequest.setMoneyPerHour(BigDecimal.valueOf(200));
        updateRequest.setMoneyPerMonth(BigDecimal.valueOf(32000));
        updateRequest.setNote("Test note updated");

        var updatedSettings = sendUpdateRequest(updateRequest)
                .andExpect(status().isOk())
                .andReturnAs(SettingsDTO.class);

        assertSettings(updateRequest, updatedSettings);
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_SettingsNotFound() {
        var request = buildSettingsRequest();
        var settings = createSettings(request);

        var updateRequest = buildSettingsRequest(settings.getId() + 1, null, null);
        sendUpdateRequest(updateRequest)
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_SettingsAreNoLongerValid() {
        var request = buildSettingsRequest(LocalDate.now().minusDays(1));
        var settings = createSettings(request);

        var updateRequest = buildSettingsRequest(settings.getId(), "Updated", LocalDate.now());
        sendUpdateRequest(updateRequest)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.SETTING_NO_LONGER_VALID));
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_UpdatedWithValidToBeforeValidFrom() {
        var request = buildSettingsRequest();
        var settings = createSettings(request);

        var updateRequest = buildSettingsRequest(settings.getId(), "Updated", LocalDate.now().minusDays(5));
        sendUpdateRequest(updateRequest)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.VALID_FROM_AFTER_VALID_TO));
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_UpdatedSettingsNameToNonUniqueNameForCurrentUser() {
        var firstRequest = buildSettingsRequest();
        var firstSettings = createSettings(firstRequest);

        var secondRequest = buildSettingsRequest(null, "TimeTracker2", LocalDate.now().plusMonths(1));
        var secondSettings = createSettings(secondRequest);

        var updateRequest = buildSettingsRequest(secondSettings.getId(), firstSettings.getName(), LocalDate.now().plusMonths(1));
        sendUpdateRequest(updateRequest)
                .andExpect(status().isUnprocessableEntity())
                .and(handleUserInputException(ErrorTypeCode.SETTING_NAME_NOT_UNIQUE));
    }

    private void assertSettings(SettingsDTO expected, SettingsDTO actual) {
        Assertions.assertNotNull(actual);

        if(expected.getId() == null) {
            Assertions.assertNotNull(actual.getId());
        } else {
            Assertions.assertEquals(expected.getId(), actual.getId());
        }

        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getNote(), actual.getNote());
        Assertions.assertEquals(expected.getValidFrom(), actual.getValidFrom());
        Assertions.assertEquals(expected.getValidTo(), actual.getValidTo());
        Assertions.assertEquals(expected.getMoneyPerHour(), actual.getMoneyPerHour());
        Assertions.assertEquals(expected.getMoneyPerMonth(), actual.getMoneyPerMonth());

    }

    private SettingsDTO createSettings(SettingsDTO request) throws Exception {
        return sendCreateRequest(request)
                .andExpect(status().isCreated())
                .andReturnAs(SettingsDTO.class);
    }

    private ResultActionsHandler sendCreateRequest(SettingsDTO request) {
        return sendCreateRequest(request, authorizationOf(primaryUser()));
    }

    private ResultActionsHandler sendCreateRequest(SettingsDTO request, UserAuthorizationContextHolder contextHolder) {
        return buildRequest(HttpMethod.POST, SETTINGS_API_BASE_PATH)
                .withAuthorization(contextHolder)
                .withBody(request)
                .performWith(mvc);
    }

    private ResultActionsHandler sendUpdateRequest(SettingsDTO request) {
        return buildRequest(HttpMethod.PUT, STR."\{SETTINGS_API_BASE_PATH}/{taskId}", request.getId())
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc);
    }

    private SettingsDTO buildSettingsRequest() {
        return buildSettingsRequest(null);
    }

    private SettingsDTO buildSettingsRequest(LocalDate validTo) {
        return buildSettingsRequest(null, null, validTo);
    }

    private SettingsDTO buildSettingsRequest(Long id, String name, LocalDate validTo) {
        var request = new SettingsDTO();
        request.setId(id);
        request.setValidFrom(LocalDate.now().minusDays(1));
        request.setValidTo(validTo);
        request.setMoneyPerHour(BigDecimal.valueOf(100));
        request.setMoneyPerMonth(BigDecimal.valueOf(16000));
        request.setName(Objects.requireNonNullElse(name, "TimeTracker1"));
        request.setNote("Test note");
        return request;
    }
}
