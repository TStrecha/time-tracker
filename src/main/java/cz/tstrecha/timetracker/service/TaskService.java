package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;

public interface TaskService {

    TaskDTO createTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser);

    TaskDTO createEmptyTask(IdentifierType identifierType, String identifierValue, LoggedUser loggedUser);
}
