package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.constant.TaskStatus;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.filter.TaskFilter;
import cz.tstrecha.timetracker.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "task-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "/task", produces = {APPLICATION_JSON_VALUE})
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasPermission(#userContext, 'task.create')")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskCreateRequestDTO task, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.createTask(task, userContext), HttpStatus.CREATED);
    }

    @PostMapping("/{identifier}/{identifierValue}")
    @PreAuthorize("hasPermission(#userContext, 'task.create')")
    public ResponseEntity<TaskDTO> createEmptyTask(@PathVariable("identifier") IdentifierType identifier,
                                                   @PathVariable("identifierValue") String identifierValue,
                                                   @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.createEmptyTask(identifier, identifierValue, userContext), HttpStatus.CREATED);
    }

    @PutMapping
    @PreAuthorize("hasPermission(#taskCreateRequestDTO.id, 'task', 'task.update')")
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskCreateRequestDTO taskCreateRequestDTO,
                                              @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.updateTask(taskCreateRequestDTO, userContext), HttpStatus.OK);
    }

    @PatchMapping("/{id}/{newStatus}")
    @PreAuthorize("hasPermission(#id, 'task', 'task.update')")
    public ResponseEntity<TaskDTO> changeTaskStatus(@PathVariable("id") Long id,
                                                    @PathVariable("newStatus") TaskStatus newStatus,
                                                    @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.changeTaskStatus(id, newStatus, userContext), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'task', 'task.update')")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable("id") Long id, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.deleteTask(id), HttpStatus.OK);
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasPermission(#id, 'task', 'task.update')")
    public ResponseEntity<TaskDTO> reactivateTask(@PathVariable("id") Long id, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.reactivateTask(id), HttpStatus.OK);
    }

    @GetMapping("/search/{query}")
    @PreAuthorize("hasPermission(#userContext, 'task.read')")
    public ResponseEntity<List<TaskDTO>> searchForTasks(@PathVariable("query") String query,
                                                        @RequestParam(defaultValue = "5", required = false) Long limit,
                                                        @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.searchForTasks(query, limit, userContext), HttpStatus.OK);
    }

    @PostMapping("/list")
    @PreAuthorize("hasPermission(#userContext, 'task.read')")
    public ResponseEntity<Page<TaskDTO>> listTasks(@RequestBody TaskFilter taskFilter, @InjectUserContext UserContext userContext){
        return new ResponseEntity<>(taskService.listTasks(taskFilter, userContext), HttpStatus.OK);
    }
}
