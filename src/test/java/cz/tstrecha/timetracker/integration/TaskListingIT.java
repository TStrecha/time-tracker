package cz.tstrecha.timetracker.integration;

import cz.tstrecha.timetracker.utils.IntegrationTest;
import cz.tstrecha.timetracker.constant.SortDirection;
import cz.tstrecha.timetracker.constant.TaskFilterField;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
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
class TaskListingIT extends IntegrationTest {

    @Autowired
    private TaskService taskService;

    private List<TaskEntity> allTasks;

    @BeforeEach
    @Transactional
    void createAllTasks(){
        allTasks = new ArrayList<>();

        allTasks.add(new TaskEntity(1L, 493826L, "Refactor Codebase", "Refactor Codebase", "Improve code quality and organization", "Refactor the codebase to improve maintainability and scalability", TaskStatus.NEW, primaryUser, 90L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(2L, 292734L, "Integrate Social Media Sharing", "Integrate Social Media Sharing", "Allow users to share content on social media", "Integrate social media sharing functionality to allow users to share content on their social media profiles", TaskStatus.DONE, primaryUser, 60L, OffsetDateTime.now().minusDays(2), OffsetDateTime.now(), false));
        allTasks.add(new TaskEntity(3L, 312765L, "Implement Two-Factor Authentication", "Implement Two-Factor Authentication", "Add an extra layer of security", "Implement two-factor authentication to protect user accounts", TaskStatus.IN_PROGRESS, primaryUser, 90L, OffsetDateTime.now().minusDays(4), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(4L, 483920L, "Integrate Payment Processing", "Integrate Payment Processing", "Allow users to process payments within the application", "Integrate payment processing to enable users to process payments directly within the application", TaskStatus.DONE, primaryUser, 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(5L, 654321L, "Implement OAuth 2.0", "Implement OAuth 2.0", "Allow users to authenticate with third-party services", "Implement OAuth 2.0 to enable users to authenticate with third-party services within the application", TaskStatus.NEW, primaryUser, 90L, OffsetDateTime.now(), OffsetDateTime.now().plusDays(-4), true));
        allTasks.add(new TaskEntity(6L, 234567L, "Create Custom Reporting", "Create Custom Reporting", "Provide users with customizable reporting options", "Create a custom reporting system to allow users to generate and export reports based on their data within the application", TaskStatus.NEW, primaryUser, 60L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(7L, 987654L, "Update User Authentication", "Update User Authentication", "Enhance security measures for user authentication", "Implement multi-factor authentication and enhance password encryption to improve user account security", TaskStatus.NEW, secondaryUser, 30L, OffsetDateTime.now().minusDays(100), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(8L, 654321L, "Implement Chat Feature", "Implement Chat Feature", "Enhance communication with real-time chat functionality", "Integrate a chat feature to allow users to communicate in real-time within the application", TaskStatus.NEW, secondaryUser, 55L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(9L, 876543L, "Optimize Database Queries", "Optimize Database Queries", "Improve performance by optimizing database queries", "Identify and optimize slow-performing database queries to enhance overall system performance", TaskStatus.NEW, tertiaryUser, 50L, OffsetDateTime.now().minusDays(12), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(10L, 543210L, "Integrate Payment Gateway", "Integrate Payment Gateway", "Enable online payments through a third-party payment gateway", "Integrate a secure payment gateway to facilitate online transactions within the application", TaskStatus.NEW, tertiaryUser, 40L, OffsetDateTime.now(), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(11L, 210987L, "Design Responsive UI", "Design Responsive UI", "Create a responsive user interface for various devices", "Implement a responsive design to ensure a seamless user experience across desktops, tablets, and mobile devices", TaskStatus.NEW, tertiaryUser, 35L, OffsetDateTime.now().minusDays(7), OffsetDateTime.now(), true));
        allTasks.add(new TaskEntity(12L, 435627L, "Navrhněte responzivní uživatelské rozhraní", "Navrhněte responzivní uživatelské rozhraní", "Vytvořte responzivní uživatelské rozhraní pro různá zařízení", "Implementujte responzivní design pro zajištění bezproblémového uživatelského zážitku na stolních počítačích, tabletech a mobilních zařízeních", TaskStatus.NEW, primaryUser, 12L, OffsetDateTime.now().minusYears(4), OffsetDateTime.now().plusDays(100), true));

        taskRepository.saveAll(allTasks);
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("filterData")
    void should_FilterTasks_When_FilterProvided(TaskFilterField taskFilterField, String value, List<Long> rightIndices) {
        TaskFilter taskFilter = new TaskFilter(Map.of(taskFilterField, value), TaskFilterField.ID, SortDirection.ASC, 5, 0);
        var tasks = taskService.listTasks(taskFilter, getPrimaryUserAuthorizationToken().getContext()).toList();

        Assertions.assertEquals(rightIndices.size(), tasks.size());
        Assertions.assertTrue(tasks.stream().map(TaskCreateRequestDTO::getId).allMatch(rightIndices::contains));
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("sortingData")
    <U extends Comparable<? super U>> void should_SortTasks_When_SortFieldProvided(TaskFilterField sortField,
                                                                                   Function<TaskEntity, ? extends U> mappingFunction,
                                                                                   SortDirection sortDirection) {
        var taskFilterAsc = new TaskFilter(Map.of(), sortField, sortDirection, Integer.MAX_VALUE, 0);
        var currentUserTasks = allTasks.stream().filter(task -> task.getUser().getId().equals(primaryUser.getId())).toList();

        Comparator<TaskEntity> comparator;
        if(sortDirection == SortDirection.ASC) {
            comparator = Comparator.comparing(mappingFunction);
        } else {
            comparator = Comparator.comparing(mappingFunction).reversed();
        }

        var tasksSortedAsc = taskService.listTasks(taskFilterAsc, getPrimaryUserAuthorizationToken().getContext()).toList();
        var sortedManuallyAsc = currentUserTasks.stream().sorted(comparator).toList();

        Assertions.assertEquals(currentUserTasks.size(), tasksSortedAsc.size());

        IntStream.of(0, tasksSortedAsc.size() - 1).forEach(i -> {
            var returnedTask = tasksSortedAsc.get(i);
            var manualSortedTask = sortedManuallyAsc.get(i);

            Assertions.assertEquals(manualSortedTask.getId(), returnedTask.getId());
        });
    }

    @Test
    @SneakyThrows
    @Transactional
    void should_SortFilteredTasks_When_SortAndFilterGiven() {
        var taskFilter = new TaskFilter(Map.of(TaskFilterField.STATUS, "NEW"), TaskFilterField.DESCRIPTION, SortDirection.DESC, Integer.MAX_VALUE, 0);

        var currentUserTasks = allTasks.stream().filter(task -> task.getUser().getId().equals(primaryUser.getId())).toList();

        var tasks = taskService.listTasks(taskFilter, getPrimaryUserAuthorizationToken().getContext());

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
    void should_ReturnPagedTasks_When_MultiplePagesAreAvailable() {
        var firstPage = new TaskFilter(Map.of(), TaskFilterField.ID, SortDirection.ASC, 5, 0);
        var secondPage = new TaskFilter(Map.of(), TaskFilterField.ID, SortDirection.ASC, 5, 1);

        var tasksFirstPage = taskService.listTasks(firstPage, getPrimaryUserAuthorizationToken().getContext());
        var tasksSecondPage = taskService.listTasks(secondPage, getPrimaryUserAuthorizationToken().getContext());

        Assertions.assertEquals(7, tasksFirstPage.getTotalElements());
        Assertions.assertEquals(2, tasksFirstPage.getTotalPages());
        Assertions.assertEquals(2, tasksSecondPage.getTotalPages());

        Assertions.assertEquals(5, tasksFirstPage.getNumberOfElements());
        Assertions.assertEquals(2, tasksSecondPage.getNumberOfElements());
    }

    @SneakyThrows
    @Transactional
    @ParameterizedTest
    @MethodSource("userData")
    void should_ReturnTasksForUserInContext_When_LoggedAsAnotherUser(Integer userIndex, Integer expectedTaskCount) {
        var authorizedAs = switch(userIndex) {
            case 0 -> primaryUser;
            case 1 -> secondaryUser;
            case 2 -> tertiaryUser;
            case 3 -> adminUser;
            default -> null;
        };

        TaskFilter taskFilter = new TaskFilter(Map.of(), TaskFilterField.ID, SortDirection.ASC, 100, 0);
        var tasks = taskService.listTasks(taskFilter, getAuthorizationToken(primaryUser, authorizedAs).getContext()).toList();

        Assertions.assertEquals(expectedTaskCount, tasks.size());
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
                Arguments.of(TaskFilterField.DESCRIPTION, "Implement", List.of(3L, 5L, 12L)),
                Arguments.of(TaskFilterField.DESCRIPTION, "Integrate payment processing to enable users", List.of(4L)),
                Arguments.of(TaskFilterField.STATUS, "NEW", List.of(1L, 5L, 6L, 12L)),
                Arguments.of(TaskFilterField.STATUS, "IN_PROGRESS", List.of(3L)),
                Arguments.of(TaskFilterField.ESTIMATE, "60", List.of(2L, 4L, 6L)),
                Arguments.of(TaskFilterField.ACTIVE, "true", List.of(1L, 3L, 4L, 5L, 6L)),
                Arguments.of(TaskFilterField.ACTIVE, "false", List.of(2L)),
                Arguments.of(TaskFilterField.NOTE, "Test blank note", List.of()),
                Arguments.of(TaskFilterField.DESCRIPTION, "Test blank description", List.of()),
                Arguments.of(TaskFilterField.NAME_SIMPLE, "uživatelské", List.of(12L)),
                Arguments.of(TaskFilterField.NOTE, "zaříz", List.of(12L))
        );
    }

    private static Stream<Arguments> sortingData() {

        Function<TaskEntity, ?> ticketIdMapper = TaskEntity::getId;
        Function<TaskEntity, ?> ticketEstimateMapper = TaskEntity::getEstimate;
        Function<TaskEntity, ?> ticketCustomIdMapper = TaskEntity::getCustomId;
        Function<TaskEntity, ?> ticketNameMapper = TaskEntity::getNameSimple;

        return Stream.of(
                Arguments.of(TaskFilterField.ID, ticketIdMapper, SortDirection.ASC),
                Arguments.of(TaskFilterField.ESTIMATE, ticketEstimateMapper, SortDirection.ASC),
                Arguments.of(TaskFilterField.CUSTOM_ID, ticketCustomIdMapper, SortDirection.ASC),
                Arguments.of(TaskFilterField.NAME_SIMPLE, ticketNameMapper, SortDirection.ASC),

                Arguments.of(TaskFilterField.ID, ticketIdMapper, SortDirection.DESC),
                Arguments.of(TaskFilterField.ESTIMATE, ticketEstimateMapper, SortDirection.DESC),
                Arguments.of(TaskFilterField.CUSTOM_ID, ticketCustomIdMapper, SortDirection.DESC),
                Arguments.of(TaskFilterField.NAME_SIMPLE, ticketNameMapper, SortDirection.DESC)
        );
    }

    private Stream<Arguments> userData() {

        return Stream.of(
                Arguments.of(0, 7),
                Arguments.of(1, 2),
                Arguments.of(2, 3),
                Arguments.of(3, 0),
                Arguments.of(4, 7)
        );
    }
}
