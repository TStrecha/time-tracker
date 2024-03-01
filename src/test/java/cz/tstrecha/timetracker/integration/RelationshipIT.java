package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.time.OffsetDateTime;
import java.util.List;

import static cz.tstrecha.timetracker.utils.RequestBuilder.buildRequest;
import static cz.tstrecha.timetracker.utils.assertions.UserInputExceptionHandler.handleUserInputException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RelationshipIT extends IntegrationTest {

    private final static String RELATIONSHIP_API_BASE_PATH = "/relationship";

    @Autowired
    private RelationshipMapper relationshipMapper;

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUnauthorized_When_CreatingNotAuthorized() {
        var duplicateRequest = createRequest(primaryUser);

        buildRequest(HttpMethod.POST, RELATIONSHIP_API_BASE_PATH)
                .withBody(duplicateRequest)
                .performWith(mvc)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_CreateRelationship_When_ValidRequestGiven() {
        var request = createRequest(secondaryUser);

        var response = buildRequest(HttpMethod.POST, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getAuthorizationToken(primaryUser))
                .withBody(request)
                .performWith(mvc)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        var createdRelationship = objectMapper.readValue(response.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(request.getToId(), createdRelationship.getOppositeUserId());
        Assertions.assertEquals(request.getPermissions(), createdRelationship.getPermissions());
        Assertions.assertEquals(secondaryUser.getDisplayName(), createdRelationship.getDisplayName());
        Assertions.assertEquals(request.getActiveFrom(), createdRelationship.getActiveFrom());
        Assertions.assertEquals(request.getActiveTo(), createdRelationship.getActiveTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeForbidden_When_CreateRelationshipAndLoggedAsDifferentUser() {
        var request = createRequest(tertiaryUser);

        buildRequest(HttpMethod.POST, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getAuthorizationToken(primaryUser, secondaryUser))
                .withBody(request)
                .performWith(mvc)
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeUserInputException_When_CreateRelationshipBetweenSameUser() {
        var request = createRequest(secondaryUser);

        buildRequest(HttpMethod.POST, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getPrimaryUserAuthorizationToken())
                .withBody(request)
                .performWith(mvc)
                .andExpect(status().isCreated());

        var duplicateRequest = createRequest(secondaryUser);
        duplicateRequest.setPermissions(List.of("task.read"));
        duplicateRequest.setSecureValues(true);

        buildRequest(HttpMethod.POST, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getPrimaryUserAuthorizationToken())
                .withBody(request)
                .performWith(mvc)
                .andExpect(status().isUnprocessableEntity())
                .andDo(handleUserInputException(ErrorTypeCode.RELATIONSHIP_ALREADY_EXISTS));
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_UpdateRelationship_When_LoggedAsOwner() {
        var request = createRequest(secondaryUser);
        var relationshipEntity = relationshipMapper.fromRequest(request, primaryUser, secondaryUser);

        relationshipRepository.save(relationshipEntity);

        var updateRequest = createUpdatedRequest(relationshipEntity.getId(), secondaryUser);

        var response = buildRequest(HttpMethod.PUT, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getPrimaryUserAuthorizationToken())
                .withBody(updateRequest)
                .performWith(mvc)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(response.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(updateRequest.getToId(), relation.getOppositeUserId());
        Assertions.assertEquals(updateRequest.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(secondaryUser.getDisplayName(), relation.getDisplayName());
        Assertions.assertEquals(updateRequest.getActiveFrom(), relation.getActiveFrom());
        Assertions.assertEquals(updateRequest.getActiveTo(), relation.getActiveTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_DoNothing_When_UserChangeRequested() {
        var request = createRequest(secondaryUser);
        var relationshipEntity = relationshipMapper.fromRequest(request, primaryUser, secondaryUser);

        relationshipRepository.save(relationshipEntity);

        var updateRequest = createUpdatedRequest(relationshipEntity.getId(), tertiaryUser);

        var response = buildRequest(HttpMethod.PUT, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getPrimaryUserAuthorizationToken())
                .withBody(updateRequest)
                .performWith(mvc)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(response.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(secondaryUser.getId(), relation.getOppositeUserId());
        Assertions.assertEquals(updateRequest.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(secondaryUser.getDisplayName(), relation.getDisplayName());
        Assertions.assertEquals(updateRequest.getActiveFrom(), relation.getActiveFrom());
        Assertions.assertEquals(updateRequest.getActiveTo(), relation.getActiveTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeForbidden_When_UpdatingNotExistingRelationship() {
        var duplicateRequest = createUpdatedRequest(Long.MAX_VALUE, secondaryUser);

        buildRequest(HttpMethod.PUT, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getPrimaryUserAuthorizationToken())
                .withBody(duplicateRequest)
                .performWith(mvc)
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_BeForbidden_When_UpdateRelationshipBelongingToDifferentUser() {
        var request = createRequest(secondaryUser);
        var relationshipEntity = relationshipMapper.fromRequest(request, tertiaryUser, primaryUser);

        relationshipRepository.save(relationshipEntity);

        var updateRequest = createUpdatedRequest(relationshipEntity.getId(), tertiaryUser);

        buildRequest(HttpMethod.PUT, RELATIONSHIP_API_BASE_PATH)
                .withAuthorization(getAuthorizationToken(secondaryUser))
                .withBody(updateRequest)
                .performWith(mvc)
                .andExpect(status().isForbidden())
                .andReturn()
                .getResponse();
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
