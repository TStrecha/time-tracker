package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.mapper.TaskMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    public TaskDTO createTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser) {
        var taskEntity = taskMapper.fromRequest(taskRequest, loggedUser.getUserEntity());
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    public TaskDTO createEmptyTask(IdentifierType identifierType, String identifierValue, LoggedUser loggedUser) {
        var request = new TaskCreateRequestDTO();
        switch (identifierType) {
            case NAME -> request.setName(identifierValue);
            case CUSTOM_ID -> request.setCustomId(identifierValue);
        }
        var taskEntity = taskMapper.fromRequest(request, loggedUser.getUserEntity());
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }


}
