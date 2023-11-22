package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {
    /**
     *
     * @param taskRequest
     * @param loggedUser
     * @return
     */
    TaskDTO createTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser);

    /**
     *
     * @param identifierType
     * @param identifierValue
     * @param loggedUser
     * @return
     */
    TaskDTO createEmptyTask(IdentifierType identifierType, String identifierValue, LoggedUser loggedUser);

    /**
     *
     * @param taskRequest
     * @param loggedUser
     * @return
     */
    TaskDTO updateTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser);

    /**
     *
     * @param id
     * @param taskStatus
     * @param loggedUser
     * @return
     */
    TaskDTO changeTaskStatus(Long id, TaskStatus taskStatus, LoggedUser loggedUser);

    /**
     *
     * @param id
     * @param loggedUser
     * @return
     */
    TaskDTO deleteTask(Long id, LoggedUser loggedUser);

    /**
     *
     * @param id
     * @param loggedUser
     * @return
     */
    TaskDTO reactivateTask(Long id, LoggedUser loggedUser);

    /**
     *
     * @param limit
     * @param query
     * @param loggedUser
     * @return
     */
    List<TaskDTO> searchForTasks(String query, Long limit, LoggedUser loggedUser);

    /**
     *
     * @param taskFilter
     * @param loggedUser
     * @return
     */
    Page<TaskDTO> listTasks(TaskFilter taskFilter, LoggedUser loggedUser);
}
