package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

class RelationshipIT extends IntegrationTest {

    @Autowired
    private RelationshipMapper relationshipMapper;

    @Test
    @SneakyThrows
    @Transactional
    void test01_createRelationship_success() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var apiResult = mvc.perform(
                post(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                userRepository.findById(userIds.getFirst()).orElseThrow(),
                                null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(apiResult.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(request.getToId(), relation.getOppositeUserId());
        Assertions.assertEquals(request.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(getDisplayName(userRepository.findById(request.getToId()).orElseThrow()), relation.getDisplayName());
        Assertions.assertEquals(request.getActiveFrom(), relation.getActiveFrom());
        Assertions.assertEquals(request.getActiveTo(), relation.getActiveTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test02_createRelationship_fail_createForOtherUser() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var loggedAs = new ContextUserDTO(userIds.getFirst() + 1, "wrong@email.com", "Wrong User", AccountType.PERSON, null, null, false);

        mvc.perform(
                    post(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX +
                                    authenticationService.generateToken(userRepository.findById(userIds.getFirst()).orElseThrow(), loggedAs)))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andReturn()
            .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    void test03_createRelationship_fail_createSameRelationship() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.getFirst()).orElseThrow(),
                                        null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var response = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.getFirst()).orElseThrow(),
                                        null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(response.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("UserInputException", exceptionDTO.getException());
        Assertions.assertEquals("Relationship already exists", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("RelationshipCreateUpdateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test04_createRelationship_fail_createForOtherUser() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.getFirst()).orElseThrow(),
                                        new ContextUserDTO())))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    void test05_updateRelationship_success() {
        var userIds = mockUsers(3);
        var userEntities = userIds.stream().map(id -> userRepository.findById(id).orElseThrow()).toList();
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var relationshipEntity = relationshipMapper.fromRequest(request,
                userEntities.getFirst(),
                userEntities.get(1));

        relationshipRepository.save(relationshipEntity);

        request.setId(relationshipEntity.getId());
        request.setPermissions(List.of(""));
        request.setSecureValues(true);

        var apiResult = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                       userEntities.getFirst(), null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(apiResult.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(request.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(getDisplayName(userEntities.get(1)), relation.getDisplayName());
        Assertions.assertEquals(request.getActiveFrom(), relation.getActiveFrom());
        Assertions.assertEquals(request.getActiveTo(), relation.getActiveTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test06_updateRelationship_fail_updateOtherUser() {
        var userIds = mockUsers(3);
        var userEntities = userIds.stream().map(id -> userRepository.findById(id).orElseThrow()).toList();
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var relationshipEntity = relationshipMapper.fromRequest(request,
                userEntities.get(2),
                userEntities.get(1));

        relationshipRepository.save(relationshipEntity);

        request.setId(relationshipEntity.getId());
        request.setPermissions(List.of("report.read"));
        request.setSecureValues(true);

        var response = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userEntities.getFirst(), null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(response.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("UserInputException", exceptionDTO.getException());
        Assertions.assertEquals("You cannot edit a relationship between other users", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("RelationshipCreateUpdateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    private String getDisplayName(UserEntity user){
        return user.getAccountType() == AccountType.PERSON ? STR."\{user.getFirstName()} \{user.getLastName()}" : user.getCompanyName();
    }
}
