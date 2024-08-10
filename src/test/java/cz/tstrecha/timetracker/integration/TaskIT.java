package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Objects;

import static cz.tstrecha.timetracker.utils.RequestBuilder.buildRequest;
import static cz.tstrecha.timetracker.utils.assertions.UserInputExceptionHandler.handleIllegalEntityStateException;

class TaskIT extends IntegrationTest {

    private final static String TASK_API_BASE_PATH = "/task";

    @Test
    @SneakyThrows
    void should_CreateTask_When_ValidRequestGiven() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withBody(request)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        assertTask(request, task);
    }

    @Test
    @SneakyThrows
    void should_CreateEmptyTask_When_ValidRequestGiven() {
        var identifier = IdentifierType.NAME;
        var name = "New Task";

        var taskDTO = buildRequest(HttpMethod.POST, STR."\{TASK_API_BASE_PATH}/{identifier}/{name}", identifier, name)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        Assertions.assertNull(taskDTO.getCustomId());
        Assertions.assertNull(taskDTO.getDescription());
        Assertions.assertNull(taskDTO.getNote());
        Assertions.assertNull(taskDTO.getEstimate());

        Assertions.assertEquals(1L, taskDTO.getId());
        Assertions.assertEquals(name, taskDTO.getName());
        Assertions.assertEquals(name, taskDTO.getNameSimple());
        Assertions.assertEquals(TaskStatus.NEW, taskDTO.getStatus());

        Assertions.assertTrue(taskDTO.isActive());
    }

    @Test
    @SneakyThrows
    void should_UpdateTask_When_ValidRequestGiven() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        request.setId(task.getId());
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        var updatedTask = buildRequest(HttpMethod.PUT, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturnAs(TaskDTO.class);

        assertTask(request, updatedTask);
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_TaskNotFound() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        request.setId(task.getId() + 1);

        buildRequest(HttpMethod.PUT, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_TaskIsNotActive() {
        var request = createTaskRequest();
        request.setActive(false);

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        request.setId(task.getId());
        request.setName("updatedTaskName");

        buildRequest(HttpMethod.PUT, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .and(handleIllegalEntityStateException(ErrorTypeCode.TASK_IS_NOT_ACTIVE));
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_TaskIsDone() {
        var request = createTaskRequest();
        request.setStatus(TaskStatus.DONE);

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        request.setId(task.getId());
        request.setCustomId(4321L);
        request.setName("updatedTaskName");
        request.setNameSimple(null);
        request.setNote("updatedTaskNote");
        request.setDescription("updatedTaskDescription");
        request.setStatus(TaskStatus.RETURNED);
        request.setEstimate(2048L);
        request.setActive(false);

        buildRequest(HttpMethod.PUT, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .and(handleIllegalEntityStateException(ErrorTypeCode.TASK_ALREADY_DONE));
    }

    @Test
    @SneakyThrows
    void should_ChangeStatus_When_QuickActionRequested() {
        var request = createTaskRequest();

        var createdTask = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        var newStatus = TaskStatus.IN_PROGRESS;

        var updatedTask = buildRequest(HttpMethod.PATCH, STR."\{TASK_API_BASE_PATH}/{taskId}/{newStatus}", createdTask.getId(), newStatus)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturnAs(TaskDTO.class);

        assertTask(request, updatedTask, createdTask.isActive(), newStatus);
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_QuickActionRequestedAndTaskNotFound() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        buildRequest(HttpMethod.PATCH, STR."\{TASK_API_BASE_PATH}/{taskId}/{newStatus}", task.getId() + 1, TaskStatus.IN_PROGRESS)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_BeUnprocessableEntity_When_QuickActionRequestedAndTaskIsNotActive() {
        var request = createTaskRequest();
        request.setActive(false);

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        buildRequest(HttpMethod.PATCH, STR."\{TASK_API_BASE_PATH}/{taskId}/{newStatus}", task.getId(), TaskStatus.IN_PROGRESS)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .and(handleIllegalEntityStateException(ErrorTypeCode.TASK_IS_NOT_ACTIVE));
    }

    @Test
    @SneakyThrows
    void should_SoftDeleteTask_When_DeleteRequested() {
        var request = createTaskRequest();

        var createdTask = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        var deletedTask = buildRequest(HttpMethod.DELETE, STR."\{TASK_API_BASE_PATH}/{taskId}", createdTask.getId())
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturnAs(TaskDTO.class);

        assertTask(request, deletedTask, false, createdTask.getStatus());
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_DeleteActionRequestedAndTaskNotFound() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        buildRequest(HttpMethod.DELETE, STR."\{TASK_API_BASE_PATH}/{taskId}", task.getId() + 1)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    void should_SetActiveToTrue_When_TaskReactivateQuickActionRequested() {
        var request = createTaskRequest();
        request.setActive(false);

        var createdTask = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        var reactivatedTask = buildRequest(HttpMethod.PATCH, STR."\{TASK_API_BASE_PATH}/{taskId}/reactivate", createdTask.getId())
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturnAs(TaskDTO.class);

        assertTask(request, reactivatedTask, true, createdTask.getStatus());
    }

    @Test
    @SneakyThrows
    void should_BeForbidden_When_TaskReactivateQuickActionRequestedAndTaskNotFound() {
        var request = createTaskRequest();

        var task = buildRequest(HttpMethod.POST, TASK_API_BASE_PATH)
                .withAuthorization(ofPrimaryUser())
                .withBody(request)
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturnAs(TaskDTO.class);

        buildRequest(HttpMethod.PATCH, STR."\{TASK_API_BASE_PATH}/{taskId}/reactivate", task.getId() + 1)
                .withAuthorization(ofPrimaryUser())
                .performWith(mvc)
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    private void assertTask(TaskCreateRequestDTO request, TaskDTO task) {
        assertTask(request, task, request.isActive(), request.getStatus());
    }

    private void assertTask(TaskCreateRequestDTO request, TaskDTO task, boolean expectedActive, TaskStatus expectedStatus) {
        if(request.getId() == null) {
            Assertions.assertNotNull(task.getId());
        } else {
            Assertions.assertEquals(request.getId(), task.getId());
        }

        Assertions.assertEquals(request.getCustomId(), task.getCustomId());
        Assertions.assertEquals(request.getName(), task.getName());
        Assertions.assertEquals(request.getNote(), task.getNote());
        Assertions.assertEquals(request.getDescription(), task.getDescription());
        Assertions.assertEquals(Objects.requireNonNullElse(expectedStatus, TaskStatus.NEW), task.getStatus());
        Assertions.assertEquals(request.getEstimate(), task.getEstimate());

        Assertions.assertEquals(expectedActive, task.isActive());

        Assertions.assertNotNull(task.getNameSimple());

        var taskEntity = taskRepository.findById(task.getId()).orElseThrow(() -> new IllegalStateException("Task was not present in the database."));
        Assertions.assertNotNull(taskEntity.getCreatedAt());
        Assertions.assertNotNull(taskEntity.getUpdatedAt());
    }

    private TaskCreateRequestDTO createTaskRequest() {
        var request = new TaskCreateRequestDTO();

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
