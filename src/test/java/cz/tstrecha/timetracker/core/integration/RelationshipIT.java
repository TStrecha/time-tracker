package cz.tstrecha.timetracker.core.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.AccountType;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.RelationshipCreateUpdateRequestDTO;
import cz.tstrecha.timetracker.dto.RelationshipDTO;
import cz.tstrecha.timetracker.dto.mapper.RelationshipMapper;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
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

public class RelationshipIT extends IntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RelationshipMapper relationshipMapper;

    @Test
    @SneakyThrows
    @Transactional
    public void test01_createRelationship_success() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(0));
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var apiResult = mvc.perform(
                post(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                userRepository.findById(userIds.get(0)).orElseThrow(),
                                null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(apiResult.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(request.getToId(), relation.getOppositeUserId());
        Assertions.assertEquals(request.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(getDisplayName(userRepository.findById(request.getToId()).orElseThrow()), relation.getDisplayName());
        Assertions.assertEquals(request.getValidFrom(), relation.getValidFrom());
        Assertions.assertEquals(request.getValidTo(), relation.getValidTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test02_createRelationship_fail_createForOtherUser() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(0)+1);
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        mvc.perform(
                    post(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(request))
                            .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                    userRepository.findById(userIds.get(0)).orElseThrow(),
                                    null)))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andReturn()
            .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test03_createRelationship_fail_createSameRelationship() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(0));
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        mvc.perform(
                        post(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.get(0)).orElseThrow(),
                                        null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        mvc.perform(
                        post(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.get(0)).orElseThrow(),
                                        null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test04_createRelationship_fail_createForOtherUser() {
        var userIds = mockUsers(2);
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(0));
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        mvc.perform(
                        post(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userRepository.findById(userIds.get(0)).orElseThrow(),
                                        new ContextUserDTO())))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test05_updateRelationship_success() {
        var userIds = mockUsers(3);
        var userEntities = userIds.stream().map(id -> userRepository.findById(id).orElseThrow()).toList();
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(0));
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var relationshipEntity = relationshipMapper.fromRequest(request,
                userEntities.get(0),
                userEntities.get(1));

        relationshipRepository.save(relationshipEntity);

        request.setId(relationshipEntity.getId());
        request.setPermissions(List.of(""));
        request.setSecureValues(true);

        var apiResult = mvc.perform(
                        put(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                       userEntities.get(0), null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var relation = objectMapper.readValue(apiResult.getContentAsString(), RelationshipDTO.class);

        Assertions.assertEquals(request.getPermissions(), relation.getPermissions());
        Assertions.assertEquals(getDisplayName(userEntities.get(1)), relation.getDisplayName());
        Assertions.assertEquals(request.getValidFrom(), relation.getValidFrom());
        Assertions.assertEquals(request.getValidTo(), relation.getValidTo());
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test06_updateRelationship_fail_updateOtherUser() {
        var userIds = mockUsers(3);
        var userEntities = userIds.stream().map(id -> userRepository.findById(id).orElseThrow()).toList();
        var request = new RelationshipCreateUpdateRequestDTO();
        request.setFromId(userIds.get(2));
        request.setToId(userIds.get(1));
        request.setPermissions(List.of("*"));
        request.setSecureValues(false);

        var relationshipEntity = relationshipMapper.fromRequest(request,
                userEntities.get(2),
                userEntities.get(1));

        relationshipRepository.save(relationshipEntity);

        request.setId(relationshipEntity.getId());
        request.setPermissions(List.of(""));
        request.setSecureValues(true);

        mvc.perform(
                        put(Constants.V1_CONTROLLER_ROOT + "/user/relationship")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(
                                        userEntities.get(0), null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();
    }

    private String getDisplayName(UserEntity user){
        return user.getAccountType() == AccountType.PERSON ? user.getFirstName() + " " + user.getLastName() : user.getCompanyName();
    }
}
