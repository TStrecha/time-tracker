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
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.dto.mapper.TaskMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
        if (Objects.requireNonNull(identifierType) == IdentifierType.NAME) {
            taskEntity.setName(identifierValue);
            taskEntity.setNameSimple(StringUtils.stripAccents(identifierValue));
        } else if (identifierType == IdentifierType.CUSTOM_ID) {
            taskEntity.setCustomId(Long.valueOf(identifierValue));
        } else {
            throw new IllegalStateException(STR."Identifier type [\{identifierType.name()}] is not supported.");
        }
        taskEntity.setStatus(TaskStatus.NEW);
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO updateTask(TaskCreateRequestDTO taskRequest, LoggedUser loggedUser) {
        var taskEntity = getTaskOrFail(taskRequest.getId(), loggedUser.getUserEntity());

        if (!taskEntity.isActive()){
            throw new IllegalEntityStateException("Task is not active", ErrorTypeCode.TASK_IS_NOT_ACTIVE, TaskCreateRequestDTO.class);
        }
        if (taskEntity.getStatus().equals(TaskStatus.DONE)){
            throw new IllegalEntityStateException("Task is already done", ErrorTypeCode.TASK_ALREADY_DONE, TaskCreateRequestDTO.class);
        }
        taskMapper.updateTask(taskRequest, taskEntity);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO changeTaskStatus(Long id, TaskStatus taskStatus, LoggedUser loggedUser) {
        var taskEntity = getTaskOrFail(id, loggedUser.getUserEntity());

        if (!taskEntity.isActive()){
            throw new IllegalEntityStateException("Task is not active", ErrorTypeCode.TASK_IS_NOT_ACTIVE, TaskCreateRequestDTO.class);
        }
        taskEntity.setStatus(taskStatus);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO deleteTask(Long id, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(id, loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException(STR."Cannot find task with id[\{id}]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, TaskCreateRequestDTO.class));
        taskEntity.setActive(false);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO reactivateTask(Long id, LoggedUser loggedUser) {
        var taskEntity = taskRepository.findByIdAndUser(id, loggedUser.getUserEntity())
                .orElseThrow(() -> new UserInputException(STR."Cannot find task with id [\{id}]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, TaskCreateRequestDTO.class));
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

        var databaseSearchQuery = STR."%\{StringUtils.strip(query)}%";
        return taskRepository.searchForTasks(databaseSearchQuery, loggedUser.getUserEntity().getId(), limit)
                .stream().map(taskMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public Page<TaskDTO> listTasks(TaskFilter taskFilter, LoggedUser loggedUser) {
        var sortDirection = Sort.Direction.valueOf(taskFilter.getSortDirection().name());
        var sort = Sort.by(sortDirection, taskFilter.getSort().getFieldName());

        var pageable = PageRequest.of(taskFilter.getPageNumber(), taskFilter.getRows(), sort);
        return taskRepository.findByFilter(taskFilter, pageable, loggedUser).map(taskMapper::toDTO);
    }

    private TaskEntity getTaskOrFail(Long id, UserEntity userEntity) {
        return taskRepository.findByIdAndUser(id, userEntity)
                .orElseThrow(() -> new UserInputException(STR."Cannot find task with id [\{id}]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, TaskCreateRequestDTO.class));
    }
}
