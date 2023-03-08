package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.mapper.TaskMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
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
        var taskEntity = new TaskEntity();
        taskEntity.setUser(loggedUser.getUserEntity());
        switch (identifierType) {
            case NAME -> taskEntity.setName(identifierValue);
            case CUSTOM_ID -> taskEntity.setCustomId(identifierValue);
        }
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }


}
