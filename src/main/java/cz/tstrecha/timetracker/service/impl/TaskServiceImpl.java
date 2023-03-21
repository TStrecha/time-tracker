package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.controller.exception.IllegalEntityStateException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.mapper.TaskMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDTO createTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser) {
        var taskEntity = taskMapper.fromRequest(taskRequest, loggedUser.getUserEntity());
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
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

    @Override
    @Transactional
    public TaskDTO updateTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(taskRequest.getId(), loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException("Cannot find task with id[" + taskRequest.getId() + "]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, "TaskCreateRequestDTO"));
        if (!taskEntity.isActive()){
            throw new IllegalEntityStateException("Task is not active", ErrorTypeCode.TASK_ISNT_ACTIVE, "TaskCreateRequestDTO");
        }
        if (taskEntity.getStatus().equals(TaskStatus.DONE)){
            throw new IllegalEntityStateException("Task is already done", ErrorTypeCode.TASK_ALREADY_DONE, "TaskCreateRequestDTO");
        }
        taskMapper.updateTask(taskRequest, taskEntity);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO changeTaskStatus(Long id, TaskStatus taskStatus, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(id, loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException("Cannot find task with id[" + id + "]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, "TaskCreateRequestDTO"));
        if (!taskEntity.isActive()){
            throw new IllegalEntityStateException("Task is not active", ErrorTypeCode.TASK_ISNT_ACTIVE, "TaskCreateRequestDTO");
        }
        taskEntity.setStatus(taskStatus);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO deleteTask(Long id, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(id, loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException("Cannot find task with id[" + id + "]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, "TaskCreateRequestDTO"));
        taskEntity.setActive(false);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO reactivateTask(Long id, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(id, loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException("Cannot find task with id[" + id + "]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, "TaskCreateRequestDTO"));
        taskEntity.setActive(true);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public List<TaskDTO> searchForTasks(String query, Long limit, LoggedUser loggedUser) {
        if (query.length() < Constants.MIN_SEARCH_LENGTH){
            return List.of();
        }
        var databaseSearchQuery = "%" + StringUtils.strip(query) + "%";
        return taskRepository.searchForTasks(databaseSearchQuery, loggedUser.getUserEntity().getId(), limit)
                .stream().map(taskMapper::toDTO).toList();
    }

}
