package cz.tstrecha.timetracker.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.utils.ResultActionsHandler;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.time.OffsetDateTime;
import java.util.List;

import static cz.tstrecha.timetracker.utils.RequestBuilder.buildRequest;
import static cz.tstrecha.timetracker.utils.assertions.UserInputExceptionHandler.handleUserInputException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelationshipIT extends IntegrationTest {

    private final static String RELATIONSHIP_API_BASE_PATH = "/relationship";

    @Test
    @SneakyThrows
    void should_BeUnauthorized_When_CreatingNotAuthorized() {
        var request = createRequest(primaryUser());

        sendRequest(HttpMethod.POST, request, null).andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void should_CreateRelationship_When_ValidRequestGiven() {
        var request = createRequest(secondaryUser());

        var createdRelationship = createRelationship(request);

        assertRelationship(request, createdRelationship);
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_CreateRelationshipAndLoggedAsDifferentUser() {
        var request = createRequest(tertiaryUser());

        sendRequest(HttpMethod.POST, request, authorizationOf(primaryUser()).loggedAs(secondaryUser())).andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_BeUserInputException_When_CreateRelationshipBetweenSameUser() {
        var request = createRequest(secondaryUser());
        createRelationship(request);

        var duplicateRequest = createRequest(secondaryUser());
        duplicateRequest.setPermissions(List.of("task.read"));
        duplicateRequest.setSecureValues(true);

        sendCreateRequest(duplicateRequest)
                .andExpect(status().isUnprocessableEntity())
                .and(handleUserInputException(ErrorTypeCode.RELATIONSHIP_ALREADY_EXISTS));
    }

    @Test
    @SneakyThrows
    void should_UpdateRelationship_When_LoggedAsOwner() {
        var request = createRequest(secondaryUser());
        var relationship = createRelationship(request);

        var updateRequest = createUpdatedRequest(relationship.getId(), secondaryUser());
        var updatedRelationship = updateRelationship(updateRequest);

        assertRelationship(updateRequest, updatedRelationship);
    }

    @Test
    @SneakyThrows
    void should_DoNothing_When_UserChangeRequested() {
        var request = createRequest(secondaryUser());
        var relationship = createRelationship(request);

        var updateRequest = createUpdatedRequest(relationship.getId(), tertiaryUser());

        var relation = updateRelationship(updateRequest);

        Assertions.assertNotNull(relation.getActiveFrom());
        Assertions.assertEquals(secondaryUser().getId(), relation.getOppositeUserId());
        Assertions.assertEquals(updateRequest.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(secondaryUser().getDisplayName(), relation.getDisplayName());
        Assertions.assertEquals(updateRequest.getActiveTo(), relation.getActiveTo());
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_UpdatingNotExistingRelationship() {
        var request = createUpdatedRequest(Long.MAX_VALUE, secondaryUser());

        sendUpdateRequest(request).andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_UpdateRelationshipBelongingToDifferentUser() {
        var request = createRequest(secondaryUser());
        var relationship = createRelationship(request);

        var updateRequest = createUpdatedRequest(relationship.getId(), tertiaryUser());

        sendRequest(HttpMethod.PUT, updateRequest, authorizationOf(secondaryUser())).andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_GetAllRelationshipsIncludingNonActiveOnes_WhenListingRequested() {
        var request = createRequest(secondaryUser());
        createRelationship(request);

        var secondRequest = createRequest(tertiaryUser());
        secondRequest.setActiveFrom(OffsetDateTime.now().minusDays(1L));
        secondRequest.setActiveTo(OffsetDateTime.now().minusDays(1L));
        createRelationship(secondRequest);

        var secondaryUserAvailableContexts = sendRequest(HttpMethod.GET, null, authorizationOf(secondaryUser()))
                .andExpect(status().isOk())
                .andReturnAs(new TypeReference<List<RelationshipDTO>>() { });

        Assertions.assertEquals(1L, secondaryUserAvailableContexts.size());

        Assertions.assertTrue(secondaryUserAvailableContexts.stream().allMatch(context -> context.getOppositeUserId().equals(secondaryUser().getId())));

        var primaryUserAvailableContexts = sendRequest(HttpMethod.GET, null, authorizationOf(primaryUser()))
                .andExpect(status().isOk())
                .andReturnAs(new TypeReference<List<RelationshipDTO>>() { });

        Assertions.assertEquals(3L, primaryUserAvailableContexts.size());

        Assertions.assertTrue(primaryUserAvailableContexts.stream().anyMatch(context -> context.getOppositeUserId().equals(primaryUser().getId())));
        Assertions.assertTrue(primaryUserAvailableContexts.stream().anyMatch(context -> context.getOppositeUserId().equals(secondaryUser().getId())));
        Assertions.assertTrue(primaryUserAvailableContexts.stream().anyMatch(context -> context.getOppositeUserId().equals(tertiaryUser().getId())));
    }

    @Test
    @SneakyThrows
    void should_ListContextToOtherUser_When_OtherUserCreatedRelationship() {
        var request = createRequest(secondaryUser());

        createRelationship(request);

        var secondaryUserAvailableContexts = buildRequest(HttpMethod.GET, STR."\{RELATIONSHIP_API_BASE_PATH}/context")
                .withAuthorization(of(secondaryUser()))
                .performWith(mvc)
                .andExpect(status().isOk())
                .andReturnAs(new TypeReference<List<ContextUserDTO>>() { });

        Assertions.assertEquals(2L, secondaryUserAvailableContexts.size());

        Assertions.assertTrue(secondaryUserAvailableContexts.stream().anyMatch(context -> context.getId().equals(secondaryUser().getId())));
        Assertions.assertTrue(secondaryUserAvailableContexts.stream().anyMatch(context -> context.getId().equals(primaryUser().getId())));

        var primaryUserAvailableContexts = buildRequest(HttpMethod.GET, STR."\{RELATIONSHIP_API_BASE_PATH}/context")
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(status().isOk())
                .andReturnAs(new TypeReference<List<ContextUserDTO>>() { });

        Assertions.assertEquals(1L, primaryUserAvailableContexts.size());

        Assertions.assertTrue(primaryUserAvailableContexts.stream().allMatch(context -> context.getId().equals(primaryUser().getId())));
    }

    @SneakyThrows
    private RelationshipDTO createRelationship(RelationshipCreateUpdateRequestDTO relationship) {
        return sendCreateRequest(relationship)
                .andExpect(status().isCreated())
                .andReturnAs(RelationshipDTO.class);
    }

    @SneakyThrows
    private RelationshipDTO updateRelationship(RelationshipCreateUpdateRequestDTO relationship) {
        return sendUpdateRequest(relationship)
                .andExpect(status().isOk())
                .andReturnAs(RelationshipDTO.class);
    }

    private ResultActionsHandler sendCreateRequest(RelationshipCreateUpdateRequestDTO relationship) {
        return sendRequest(HttpMethod.POST, relationship, authorizationOf(primaryUser()));
    }

    private ResultActionsHandler sendUpdateRequest(RelationshipCreateUpdateRequestDTO relationship) {
        return sendRequest(HttpMethod.PUT, relationship, authorizationOf(primaryUser()));
    }

    private ResultActionsHandler sendRequest(HttpMethod method, RelationshipCreateUpdateRequestDTO relationship,
                                             UserAuthorizationContextHolder contextHolder) {
        return buildRequest(method, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(contextHolder)
                .withBody(relationship)
                .performWith(mvc);
    }

    private void assertRelationship(RelationshipCreateUpdateRequestDTO request, RelationshipDTO relationship) {
        Assertions.assertNotNull(relationship.getActiveFrom());
        Assertions.assertEquals(request.getToId(), relationship.getOppositeUserId());
        Assertions.assertEquals(request.getPermissions(), relationship.getPermissions());
        Assertions.assertEquals(secondaryUser().getDisplayName(), relationship.getDisplayName());
        Assertions.assertEquals(request.getActiveTo(), relationship.getActiveTo());
    }

    private RelationshipCreateUpdateRequestDTO createRequest(UserEntity to) {
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(to.getId());
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);
        return request;
    }

    private RelationshipCreateUpdateRequestDTO createUpdatedRequest(Long relationshipId, UserEntity to) {
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setId(relationshipId);
        request.setToId(to.getId());
        request.setPermissions(List.of(""));
        request.setSecureValues(true);
        request.setActiveTo(OffsetDateTime.now());
        return request;
    }
}
