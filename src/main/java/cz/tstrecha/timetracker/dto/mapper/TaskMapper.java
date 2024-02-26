package cz.tstrecha.timetracker.dto.mapper;

import cz.tstrecha.timetracker.dto.TaskCreateRequestDTO;
import cz.tstrecha.timetracker.dto.TaskDTO;
import cz.tstrecha.timetracker.repository.entity.TaskEntity;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(imports = { StringUtils.class })
public interface TaskMapper {

    TaskDTO toDTO(TaskEntity taskEntity);

    @Mapping(target = "id", source = "task.id")
    @Mapping(target = "active", source = "task.active")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "nameSimple", expression = "java(StringUtils.stripAccents(task.getName()))")
    TaskEntity fromRequest(TaskCreateRequestDTO task, UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nameSimple", expression = "java(StringUtils.stripAccents(request.getName()))")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTask(TaskCreateRequestDTO request, @MappingTarget TaskEntity target);
}
