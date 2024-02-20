package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

class TaskIT extends IntegrationTest {

    @Test
    @SneakyThrows
    @Transactional
    void test01_createTask_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        var apiResult = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var taskDTO = objectMapper.readValue(apiResult.getContentAsString(), TaskDTO.class);

        assertEquals(1L, taskDTO.getId());
        assertEquals(1234L, taskDTO.getCustomId());
        assertEquals("taskName", taskDTO.getName());
        assertEquals("taskName", taskDTO.getNameSimple());
        assertEquals("taskNote", taskDTO.getNote());
        assertEquals("taskDescription", taskDTO.getDescription());
        assertEquals(TaskStatus.NEW, taskDTO.getStatus());
        assertEquals(5000L, taskDTO.getEstimate());
        assertEquals(taskDTO.getCreatedAt(), taskDTO.getUpdatedAt());
        assertTrue(taskDTO.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test02_createEmptyTask_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var identifier = IdentifierType.NAME;
        var name = "someName";

        var apiResult = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task/\{identifier.name()}/\{name}")
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var taskDTO = objectMapper.readValue(apiResult.getContentAsString(), TaskDTO.class);

        assertEquals(1L, taskDTO.getId());
        assertNull(taskDTO.getCustomId());
        assertEquals(name, taskDTO.getName());
        assertEquals(name, taskDTO.getNameSimple());
        assertNull(taskDTO.getNote());
        assertNull(taskDTO.getDescription());
        assertEquals(TaskStatus.NEW, taskDTO.getStatus());
        assertNull(taskDTO.getEstimate());
        assertEquals(taskDTO.getCreatedAt(), taskDTO.getUpdatedAt());
        assertTrue(taskDTO.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test03_updateTask_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        request.setId(1L);
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        var apiResultUpdate = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var updatedTaskDTO = objectMapper.readValue(apiResultUpdate.getContentAsString(), TaskDTO.class);

        assertEquals(1L, updatedTaskDTO.getId());
        assertEquals(4321L, updatedTaskDTO.getCustomId());
        assertEquals("updatedTaskName", updatedTaskDTO.getName());
        assertEquals("updatedTaskName", updatedTaskDTO.getNameSimple());
        assertEquals("updatedTaskNote", updatedTaskDTO.getNote());
        assertEquals("updatedTaskDescription", updatedTaskDTO.getDescription());
        assertEquals(TaskStatus.RETURNED, updatedTaskDTO.getStatus());
        assertEquals(2048L, updatedTaskDTO.getEstimate());
        assertTrue(updatedTaskDTO.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test04_updateTask_fail_taskNotFound() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        request.setId(2L);
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        var response = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(response.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("UserInputException", exceptionDTO.getException());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test05_updateTask_fail_taskNotActive() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();
        request.setActive(false);

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        request.setId(1L);
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        var response = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(response.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("IllegalEntityStateException", exceptionDTO.getException());
        Assertions.assertEquals("Task is not active", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test06_updateTask_fail_taskAlreadyDone() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();
        request.setStatus(TaskStatus.DONE);

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        request.setId(1L);
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        var response = mvc.perform(
                        put(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(response.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("IllegalEntityStateException", exceptionDTO.getException());
        Assertions.assertEquals("Task is already done", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test07_changeTaskStatus_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        var createdTaskResponse = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var newStatus = TaskStatus.IN_PROGRESS;

        var updatedTaskResponse = mvc.perform(
                        patch(STR."\{Constants.V1_CONTROLLER_ROOT}/task/1/\{newStatus.name()}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var createdTask = objectMapper.readValue(createdTaskResponse.getContentAsString(), TaskDTO.class);
        var updatedTask = objectMapper.readValue(updatedTaskResponse.getContentAsString(), TaskDTO.class);

        assertEquals(createdTask.getId(), updatedTask.getId());
        assertEquals(createdTask.getCustomId(), updatedTask.getCustomId());
        assertEquals(createdTask.getName(), updatedTask.getName());
        assertEquals(createdTask.getNameSimple(), updatedTask.getNameSimple());
        assertEquals(createdTask.getNote(), updatedTask.getNote());
        assertEquals(createdTask.getDescription(), updatedTask.getDescription());
        assertEquals(newStatus, updatedTask.getStatus());
        assertEquals(createdTask.getEstimate(), updatedTask.getEstimate());
        assertEquals(createdTask.isActive(), updatedTask.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test08_changeTaskStatus_fail_taskNotFound() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var newStatus = TaskStatus.IN_PROGRESS;

        var updatedTaskResponse = mvc.perform(
                        patch(STR."\{Constants.V1_CONTROLLER_ROOT}/task/2/\{newStatus.name()}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(updatedTaskResponse.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("UserInputException", exceptionDTO.getException());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test09_changeTaskStatus_fail_taskNotActive() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();
        request.setActive(false);

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var newStatus = TaskStatus.IN_PROGRESS;

        var updatedTaskResponse = mvc.perform(
                        patch(STR."\{Constants.V1_CONTROLLER_ROOT}/task/1/\{newStatus.name()}")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(updatedTaskResponse.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("IllegalEntityStateException", exceptionDTO.getException());
        Assertions.assertEquals("Task is not active", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test10_deleteTask_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        var createdTaskResponse = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var deletedTaskResponse = mvc.perform(
                        delete(STR."\{Constants.V1_CONTROLLER_ROOT}/task/1")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var createdTask = objectMapper.readValue(createdTaskResponse.getContentAsString(), TaskDTO.class);
        var deletedTask = objectMapper.readValue(deletedTaskResponse.getContentAsString(), TaskDTO.class);

        assertEquals(createdTask.getId(), deletedTask.getId());
        assertEquals(createdTask.getCustomId(), deletedTask.getCustomId());
        assertEquals(createdTask.getName(), deletedTask.getName());
        assertEquals(createdTask.getNameSimple(), deletedTask.getNameSimple());
        assertEquals(createdTask.getNote(), deletedTask.getNote());
        assertEquals(createdTask.getDescription(), deletedTask.getDescription());
        assertEquals(createdTask.getStatus(), deletedTask.getStatus());
        assertEquals(createdTask.getEstimate(), deletedTask.getEstimate());
        assertFalse(deletedTask.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test11_deleteTask_fail_taskNotFound() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        mvc.perform(
                        delete(STR."\{Constants.V1_CONTROLLER_ROOT}/task/2")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();
    }

    @Test
    @SneakyThrows
    @Transactional
    void test12_reactivateTask_success() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();
        request.setActive(false);

        var createdTaskResponse = mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var reactivatedTask = mvc.perform(
                        patch(STR."\{Constants.V1_CONTROLLER_ROOT}/task/1/reactivate")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();

        var createdTask = objectMapper.readValue(createdTaskResponse.getContentAsString(), TaskDTO.class);
        var deletedTask = objectMapper.readValue(reactivatedTask.getContentAsString(), TaskDTO.class);

        assertFalse(createdTask.isActive());

        assertEquals(createdTask.getId(), deletedTask.getId());
        assertEquals(createdTask.getCustomId(), deletedTask.getCustomId());
        assertEquals(createdTask.getName(), deletedTask.getName());
        assertEquals(createdTask.getNameSimple(), deletedTask.getNameSimple());
        assertEquals(createdTask.getNote(), deletedTask.getNote());
        assertEquals(createdTask.getDescription(), deletedTask.getDescription());
        assertEquals(createdTask.getStatus(), deletedTask.getStatus());
        assertEquals(createdTask.getEstimate(), deletedTask.getEstimate());
        assertTrue(deletedTask.isActive());
    }

    @Test
    @SneakyThrows
    @Transactional
    void test13_reactivateTask_fail_taskNotFound() {
        var user = userRepository.findById(mockUsers(1).getFirst()).orElseThrow();
        var request = createTaskRequest();

        mvc.perform(
                        post(STR."\{Constants.V1_CONTROLLER_ROOT}/task")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse();

        var deletedTaskResponse = mvc.perform(
                        patch(STR."\{Constants.V1_CONTROLLER_ROOT}/task/2/reactivate")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + authenticationService.generateToken(user, null)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andReturn()
                .getResponse();

        var exceptionDTO = objectMapper.readValue(deletedTaskResponse.getContentAsString(), InternalErrorDTO.class);
        Assertions.assertEquals("UserInputException", exceptionDTO.getException());
        Assertions.assertEquals("Cannot find task with id [2]", exceptionDTO.getExceptionMessage());
        Assertions.assertEquals("TaskCreateRequestDTO", exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }

    private TaskCreateRequestDTO createTaskRequest() {
        var request = new TaskCreateRequestDTO();
        request.setId(0L);
        request.setCustomId(1234L);
        request.setName("taskName");
        request.setNameSimple(null);
        request.setNote("taskNote");
        request.setDescription("taskDescription");
        request.setStatus(TaskStatus.NEW);
        request.setEstimate(5000L);
        request.setActive(true);

        return request;
    }
}
