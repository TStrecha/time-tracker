package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;

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
     * @param limit
     * @param query
     * @param loggedUser
     * @return
     */
    List<TaskDTO> searchInTasks(Long limit, String query, LoggedUser loggedUser);
}
