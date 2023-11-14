package cz.tstrecha.timetracker.core.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.constant.SortDirection;
import cz.tstrecha.timetracker.constant.TaskField;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskFilter;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaskListingIT extends IntegrationTest {

    @Autowired
    private TaskService taskService;

    private List<TaskEntity> allTasks;
    private List<UserEntity> users;

    @BeforeAll
    @Transactional
    public void createAllTasks(){
        allTasks = new ArrayList<>();

        users = mockUsers(5).stream().map(id -> userRepository.findById(id).orElseThrow()).toList();

        allTasks.add(new TaskEntity(1L, 493826L, "Refactor Codebase", "Refactor Codebase", "Improve code quality and organization", "Refactor the codebase to improve maintainability and scalability", TaskStatus.NEW, users.get(0), 90L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(2L, 292734L, "Integrate Social Media Sharing", "Integrate Social Media Sharing", "Allow users to share content on social media", "Integrate social media sharing functionality to allow users to share content on their social media profiles", TaskStatus.DONE, users.get(0), 60L, OffsetDateTime.now(), OffsetDateTime.now(), false));
        allTasks.add(new TaskEntity(3L, 312765L, "Implement Two-Factor Authentication", "Implement Two-Factor Authentication", "Add an extra layer of security", "Implement two-factor authentication to protect user accounts", TaskStatus.IN_PROGRESS, users.get(0), 90L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(4L, 483920L, "Integrate Payment Processing", "Integrate Payment Processing", "Allow users to process payments within the application", "Integrate payment processing to enable users to process payments directly within the application", TaskStatus.DONE, users.get(0), 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(5L, 654321L, "Implement OAuth 2.0", "Implement OAuth 2.0", "Allow users to authenticate with third-party services", "Implement OAuth 2.0 to enable users to authenticate with third-party services within the application", TaskStatus.NEW, users.get(0), 90L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(6L, 234567L, "Create Custom Reporting", "Create Custom Reporting", "Provide users with customizable reporting options", "Create a custom reporting system to allow users to generate and export reports based on their data within the application", TaskStatus.NEW, users.get(0), 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));

        taskRepository.saveAll(allTasks);
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("filterData")
    public void test01_listTasks_filtering(TaskField taskField, String value, List<Long> rightIndices) {
        TaskFilter taskFilter = new TaskFilter(Map.of(taskField, value), TaskField.ID, SortDirection.ASC, 5, 0);
        var user = users.get(0);
        var tasks = taskService.listTasks(taskFilter, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        Assertions.assertEquals(rightIndices.size(), tasks.size());
        Assertions.assertTrue(tasks.stream().map(TaskCreateRequestDTO::getId).allMatch(rightIndices::contains));
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("sortingData")
    public <U extends Comparable<? super U>> void test02_listTasks_sorting(TaskField taskField,
                                                                           Function<TaskEntity, ? extends U> mappingFunction) {
        TaskFilter taskFilterAsc = new TaskFilter(Map.of(), taskField, SortDirection.ASC, Integer.MAX_VALUE, 0);
        var user = users.get(0);
        var tasksSortedAsc = taskService.listTasks(taskFilterAsc, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));
        var sortedManuallyAsc = allTasks.stream().sorted(Comparator.comparing(mappingFunction)).toList();

        Assertions.assertEquals(allTasks.size(), tasksSortedAsc.size());

        IntStream.of(0, tasksSortedAsc.size()-1).forEach(i -> {
            var automaticValue = tasksSortedAsc.get(i);
            var manualValue = sortedManuallyAsc.get(i);

            Assertions.assertEquals(automaticValue.getId(), manualValue.getId());
        });
    }

    private static Stream<Arguments> filterData(){
        return Stream.of(
                Arguments.of(TaskField.ID, "2", List.of(2L)),
                Arguments.of(TaskField.CUSTOM_ID, "9", List.of(1L,2L,4L)),
                Arguments.of(TaskField.CUSTOM_ID, "234567", List.of(6L)),
                Arguments.of(TaskField.NAME_SIMPLE, "Implement", List.of(3L,5L)),
                Arguments.of(TaskField.NAME_SIMPLE, "Create Custom Reporting", List.of(6L)),
                Arguments.of(TaskField.NOTE, "Allow", List.of(2L,4L,5L)),
                Arguments.of(TaskField.NOTE, "Allow users to authenticate with third-party services", List.of(5L)),
                Arguments.of(TaskField.DESCRIPTION, "Implement", List.of(3L,5L)),
                Arguments.of(TaskField.DESCRIPTION, "Integrate payment processing to enable users", List.of(4L)),
                Arguments.of(TaskField.STATUS, "NEW", List.of(1L,5L,6L)),
                Arguments.of(TaskField.STATUS, "IN_PROGRESS", List.of(3L)),
                Arguments.of(TaskField.ESTIMATE, "60", List.of(2L,4L,6L)),
                Arguments.of(TaskField.ACTIVE, "true", List.of(1L,3L,4L,5L,6L)),
                Arguments.of(TaskField.ACTIVE, "false", List.of(2L)),
                Arguments.of(TaskField.NOTE, "Test blank note", List.of()),
                Arguments.of(TaskField.DESCRIPTION, "Test blank description", List.of())
        );
    }

    private static Stream<Arguments> sortingData() {

        Function<TaskEntity, ?> ticketIdMapper = TaskEntity::getId;
        Function<TaskEntity, ?> ticketEstimateMapper = TaskEntity::getEstimate;
        Function<TaskEntity, ?> ticketCustomIdMapper = TaskEntity::getCustomId;
        Function<TaskEntity, ?> ticketNameMapper = TaskEntity::getNameSimple;
        Function<TaskEntity, ?> ticketUpdatedAtMapper = TaskEntity::getUpdatedAt;

        return Stream.of(
                Arguments.of(TaskField.ID, ticketIdMapper),
                Arguments.of(TaskField.ESTIMATE, ticketEstimateMapper),
                Arguments.of(TaskField.CUSTOM_ID, ticketCustomIdMapper),
                Arguments.of(TaskField.NAME_SIMPLE, ticketNameMapper),
                Arguments.of(TaskField.UPDATED_AT, ticketUpdatedAtMapper)
        );
    }

    private static class test {

    }
}
