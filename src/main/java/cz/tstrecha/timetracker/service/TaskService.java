package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {
    /**
     * @param taskRequest
     * @param userContext
     * @return
     */
    TaskDTO createTask(TaskCreateRequestDTO taskRequest, UserContext userContext);

    /**
     * @param identifierType
     * @param identifierValue
     * @param userContext
     * @return
     */
    TaskDTO createEmptyTask(IdentifierType identifierType, String identifierValue, UserContext userContext);

    /**
     * @param taskRequest
     * @param userContext
     * @return
     */
    TaskDTO updateTask(TaskCreateRequestDTO taskRequest, UserContext userContext);

    /**
     * @param id
     * @param taskStatus
     * @param userContext
     * @return
     */
    TaskDTO changeTaskStatus(Long id, TaskStatus taskStatus, UserContext userContext);

    /**
     * @param id
     * @return
     */
    TaskDTO deleteTask(Long id);

    /**
     * @param id
     * @return
     */
    TaskDTO reactivateTask(Long id);

    /**
     * @param query
     * @param limit
     * @param userContext
     * @return
     */
    List<TaskDTO> searchForTasks(String query, Long limit, UserContext userContext);

    /**
     * @param taskFilter
     * @param userContext
     * @return
     */
    Page<TaskDTO> listTasks(TaskFilter taskFilter, UserContext userContext);
}
