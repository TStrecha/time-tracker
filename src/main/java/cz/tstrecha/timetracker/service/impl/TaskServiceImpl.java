package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.controller.exception.IllegalEntityStateException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.dto.mapper.TaskMapper;
import cz.tstrecha.timetracker.repository.TaskRepository;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.service.TaskService;
import cz.tstrecha.timetracker.service.UserService;
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

    private final UserService userService;

    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskDTO createTask(TaskCreateRequestDTO taskRequest, UserContext userContext) {
        var user = userService.getUserFromContext(userContext);
        var taskEntity = taskMapper.fromRequest(taskRequest, user);
        taskEntity = taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO createEmptyTask(IdentifierType identifierType, String identifierValue, UserContext userContext) {
        var user = userService.getUserFromContext(userContext);

        var taskEntity = new TaskEntity();
        taskEntity.setUser(user);

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
    public TaskDTO updateTask(TaskCreateRequestDTO taskRequest, UserContext userContext) {
        var taskEntity = getTaskOrFail(taskRequest.getId());

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
    public TaskDTO changeTaskStatus(Long id, TaskStatus taskStatus, UserContext userContext) {
        var taskEntity = getTaskOrFail(id);

        if (!taskEntity.isActive()){
            throw new IllegalEntityStateException("Task is not active", ErrorTypeCode.TASK_IS_NOT_ACTIVE, TaskCreateRequestDTO.class);
        }
        taskEntity.setStatus(taskStatus);
        taskRepository.save(taskEntity);
        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO deleteTask(Long id) {
        var taskEntity = getTaskOrFail(id);

        taskEntity.setActive(false);
        taskRepository.save(taskEntity);

        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public TaskDTO reactivateTask(Long id) {
        var taskEntity = getTaskOrFail(id);

        taskEntity.setActive(true);
        taskRepository.save(taskEntity);

        return taskMapper.toDTO(taskEntity);
    }

    @Override
    @Transactional
    public List<TaskDTO> searchForTasks(String query, Long limit, UserContext userContext) {
        if (query.length() < Constants.MIN_SEARCH_LENGTH){
            return List.of();
        }

        var databaseSearchQuery = STR."%\{StringUtils.strip(query)}%";
        return taskRepository.searchForTasks(databaseSearchQuery, userContext.getCurrentUserId(), limit)
                .stream().map(taskMapper::toDTO).toList();
    }

    @Override
    @Transactional
    public Page<TaskDTO> listTasks(TaskFilter taskFilter, UserContext userContext) {
        var sortDirection = Sort.Direction.valueOf(taskFilter.getSortDirection().name());
        var sort = Sort.by(sortDirection, taskFilter.getSort().getFieldName());

        var pageable = PageRequest.of(taskFilter.getPageNumber(), taskFilter.getRows(), sort);
        return taskRepository.findByFilter(taskFilter, pageable, userContext).map(taskMapper::toDTO);
    }

    private TaskEntity getTaskOrFail(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new UserInputException(STR."Cannot find task with id [\{id}]",
                        ErrorTypeCode.TASK_NOT_FOUND_BY_ID, TaskCreateRequestDTO.class));
    }
}
