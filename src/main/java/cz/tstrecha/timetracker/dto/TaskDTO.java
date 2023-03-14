package cz.tstrecha.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO extends TaskCreateRequestDTO {

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
