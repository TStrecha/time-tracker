package cz.tstrecha.timetracker.controller.v1;

import cz.tstrecha.timetracker.annotation.InjectLoggedUser;
import cz.tstrecha.timetracker.constant.Constants;
import cz.tstrecha.timetracker.constant.IdentifierType;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "task-api")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = Constants.V1_CONTROLLER_ROOT + "task", produces = {APPLICATION_JSON_VALUE})
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskCreateRequestDTO task, @InjectLoggedUser LoggedUser user){
        return new ResponseEntity<>(taskService.createTask(task,user), HttpStatus.CREATED);
    }

    @PostMapping("/{identifier}/{identifierValue}")
    public ResponseEntity<TaskDTO> createEmptyTask(@PathVariable IdentifierType identifier,
                                                   @PathVariable String identifierValue,
                                                   @InjectLoggedUser LoggedUser user){
        return new ResponseEntity<>(taskService.createEmptyTask(identifier,identifierValue,user), HttpStatus.CREATED);
    }
}
