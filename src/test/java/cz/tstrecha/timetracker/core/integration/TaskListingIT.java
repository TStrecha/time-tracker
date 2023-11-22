package cz.tstrecha.timetracker.core.integration;

import cz.tstrecha.timetracker.IntegrationTest;
import cz.tstrecha.timetracker.constant.SortDirection;
import cz.tstrecha.timetracker.constant.TaskFilterField;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    @Transactional
    public void createAllTasks(){
        allTasks = new ArrayList<>();

        users = mockUsers(5).stream().map(id -> userRepository.findById(id).orElseThrow()).toList();

        allTasks.add(new TaskEntity(1L, 493826L, "Refactor Codebase", "Refactor Codebase", "Improve code quality and organization", "Refactor the codebase to improve maintainability and scalability", TaskStatus.NEW, users.get(0), 90L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(2L, 292734L, "Integrate Social Media Sharing", "Integrate Social Media Sharing", "Allow users to share content on social media", "Integrate social media sharing functionality to allow users to share content on their social media profiles", TaskStatus.DONE, users.get(0), 60L, OffsetDateTime.now().minusDays(2), OffsetDateTime.now(), false));
        allTasks.add(new TaskEntity(3L, 312765L, "Implement Two-Factor Authentication", "Implement Two-Factor Authentication", "Add an extra layer of security", "Implement two-factor authentication to protect user accounts", TaskStatus.IN_PROGRESS, users.get(0), 90L, OffsetDateTime.now().minusDays(4), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(4L, 483920L, "Integrate Payment Processing", "Integrate Payment Processing", "Allow users to process payments within the application", "Integrate payment processing to enable users to process payments directly within the application", TaskStatus.DONE, users.get(0), 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(5L, 654321L, "Implement OAuth 2.0", "Implement OAuth 2.0", "Allow users to authenticate with third-party services", "Implement OAuth 2.0 to enable users to authenticate with third-party services within the application", TaskStatus.NEW, users.get(0), 90L, OffsetDateTime.now(), OffsetDateTime.now().plusDays(-4), true));
        allTasks.add(new TaskEntity(6L, 234567L, "Create Custom Reporting", "Create Custom Reporting", "Provide users with customizable reporting options", "Create a custom reporting system to allow users to generate and export reports based on their data within the application", TaskStatus.NEW, users.get(0), 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(7L, 987654L, "Update User Authentication", "Update User Authentication", "Enhance security measures for user authentication", "Implement multi-factor authentication and enhance password encryption to improve user account security", TaskStatus.NEW, users.get(1), 30L, OffsetDateTime.now().minusDays(100), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(8L, 654321L, "Implement Chat Feature", "Implement Chat Feature", "Enhance communication with real-time chat functionality", "Integrate a chat feature to allow users to communicate in real-time within the application", TaskStatus.NEW, users.get(2), 55L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(9L, 876543L, "Optimize Database Queries", "Optimize Database Queries", "Improve performance by optimizing database queries", "Identify and optimize slow-performing database queries to enhance overall system performance", TaskStatus.NEW, users.get(2), 50L, OffsetDateTime.now().minusDays(12), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(10L, 543210L, "Integrate Payment Gateway", "Integrate Payment Gateway", "Enable online payments through a third-party payment gateway", "Integrate a secure payment gateway to facilitate online transactions within the application", TaskStatus.NEW, users.get(3), 40L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(11L, 210987L, "Design Responsive UI", "Design Responsive UI", "Create a responsive user interface for various devices", "Implement a responsive design to ensure a seamless user experience across desktops, tablets, and mobile devices", TaskStatus.NEW, users.get(3), 35L, OffsetDateTime.now().minusDays(7), OffsetDateTime.now(), true));

        taskRepository.saveAll(allTasks);
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("filterData")
    public void test01_listTasks_filtering(TaskFilterField taskFilterField, String value, List<Long> rightIndices) {
        TaskFilter taskFilter = new TaskFilter(Map.of(taskFilterField, value), TaskFilterField.ID, SortDirection.ASC, 5, 0);
        var user = users.get(0);
        var tasks = taskService.listTasks(taskFilter, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)).toList();

        Assertions.assertEquals(rightIndices.size(), tasks.size());
        Assertions.assertTrue(tasks.stream().map(TaskCreateRequestDTO::getId).allMatch(rightIndices::contains));
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("sortingData")
    public <U extends Comparable<? super U>> void test02_listTasks_sorting(TaskFilterField sortField,
                                                                           Function<TaskEntity, ? extends U> mappingFunction,
                                                                           boolean asc) {
        var taskFilterAsc = new TaskFilter(Map.of(), sortField, asc ? SortDirection.ASC : SortDirection.DESC, Integer.MAX_VALUE, 0);
        var user = users.get(0);
        var currentUserTasks = allTasks.stream().filter(task -> task.getUser().getId().equals(user.getId())).toList();

        Comparator<TaskEntity> comparator;
        if(asc) {
            comparator = Comparator.comparing(mappingFunction);
        } else {
            comparator = Comparator.comparing(mappingFunction).reversed();
        }

        var tasksSortedAsc = taskService.listTasks(taskFilterAsc, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user)).toList();
        var sortedManuallyAsc = currentUserTasks.stream().sorted(comparator).toList();

        Assertions.assertEquals(currentUserTasks.size(), tasksSortedAsc.size());

        IntStream.of(0, tasksSortedAsc.size() - 1).forEach(i -> {
            var automaticValue = tasksSortedAsc.get(i);
            var manualValue = sortedManuallyAsc.get(i);

            Assertions.assertEquals(automaticValue.getId(), manualValue.getId());
        });
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test03_listTasks_sortFiltered() {
        var taskFilter = new TaskFilter(Map.of(TaskFilterField.STATUS, "NEW"), TaskFilterField.DESCRIPTION, SortDirection.DESC, Integer.MAX_VALUE, 0);

        var user = users.get(0);
        var currentUserTasks = allTasks.stream().filter(task -> task.getUser().getId().equals(user.getId())).toList();

        var tasks = taskService.listTasks(taskFilter, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        var manuallySorted = currentUserTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.NEW)
                .sorted(Comparator.comparing(TaskEntity::getDescription).reversed())
                .toList();

        IntStream.of(0, manuallySorted.size() - 1).forEach(i -> {
            var automaticValue = tasks.getContent().get(i);
            var manualValue = manuallySorted.get(i);

            Assertions.assertEquals(automaticValue.getId(), manualValue.getId());
        });
    }

    @Test
    @SneakyThrows
    @Transactional
    public void test04_listTasks_pages() {
        var firstPage = new TaskFilter(Map.of(), TaskFilterField.ID, SortDirection.ASC, 5, 0);
        var secondPage = new TaskFilter(Map.of(), TaskFilterField.ID, SortDirection.ASC, 5, 1);

        var user = users.get(0);

        var tasksFirstPage = taskService.listTasks(firstPage, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));
        var tasksSecondPage = taskService.listTasks(secondPage, userMapper.toLoggedUser(userMapper.userRelationshipEntityToContextUserDTO(user.getUserRelationshipReceiving().get(0)), user));

        Assertions.assertEquals(tasksFirstPage.getTotalElements(), 6);
        Assertions.assertEquals(tasksFirstPage.getTotalPages(), 2);
        Assertions.assertEquals(tasksSecondPage.getTotalPages(), 2);

        Assertions.assertEquals(tasksFirstPage.getNumberOfElements(), 5);
        Assertions.assertEquals(tasksSecondPage.getNumberOfElements(), 1);
    }

    private static Stream<Arguments> filterData(){
        return Stream.of(
                Arguments.of(TaskFilterField.ID, "2", List.of(2L)),
                Arguments.of(TaskFilterField.CUSTOM_ID, "9", List.of(1L, 2L, 4L)),
                Arguments.of(TaskFilterField.CUSTOM_ID, "234567", List.of(6L)),
                Arguments.of(TaskFilterField.NAME_SIMPLE, "Implement", List.of(3L, 5L)),
                Arguments.of(TaskFilterField.NAME_SIMPLE, "Create Custom Reporting", List.of(6L)),
                Arguments.of(TaskFilterField.NOTE, "Allow", List.of(2L, 4L, 5L)),
                Arguments.of(TaskFilterField.NOTE, "Allow users to authenticate with third-party services", List.of(5L)),
                Arguments.of(TaskFilterField.DESCRIPTION, "Implement", List.of(3L, 5L)),
                Arguments.of(TaskFilterField.DESCRIPTION, "Integrate payment processing to enable users", List.of(4L)),
                Arguments.of(TaskFilterField.STATUS, "NEW", List.of(1L, 5L, 6L)),
                Arguments.of(TaskFilterField.STATUS, "IN_PROGRESS", List.of(3L)),
                Arguments.of(TaskFilterField.ESTIMATE, "60", List.of(2L, 4L, 6L)),
                Arguments.of(TaskFilterField.ACTIVE, "true", List.of(1L, 3L, 4L, 5L, 6L)),
                Arguments.of(TaskFilterField.ACTIVE, "false", List.of(2L)),
                Arguments.of(TaskFilterField.NOTE, "Test blank note", List.of()),
                Arguments.of(TaskFilterField.DESCRIPTION, "Test blank description", List.of())
        );
    }

    private static Stream<Arguments> sortingData() {

        Function<TaskEntity, ?> ticketIdMapper = TaskEntity::getId;
        Function<TaskEntity, ?> ticketEstimateMapper = TaskEntity::getEstimate;
        Function<TaskEntity, ?> ticketCustomIdMapper = TaskEntity::getCustomId;
        Function<TaskEntity, ?> ticketNameMapper = TaskEntity::getNameSimple;

        return Stream.of(
                Arguments.of(TaskFilterField.ID, ticketIdMapper, true),
                Arguments.of(TaskFilterField.ESTIMATE, ticketEstimateMapper, true),
                Arguments.of(TaskFilterField.CUSTOM_ID, ticketCustomIdMapper, true),
                Arguments.of(TaskFilterField.NAME_SIMPLE, ticketNameMapper, true),

                Arguments.of(TaskFilterField.ID, ticketIdMapper, false),
                Arguments.of(TaskFilterField.ESTIMATE, ticketEstimateMapper, false),
                Arguments.of(TaskFilterField.CUSTOM_ID, ticketCustomIdMapper, false),
                Arguments.of(TaskFilterField.NAME_SIMPLE, ticketNameMapper, false)
        );
    }
}
