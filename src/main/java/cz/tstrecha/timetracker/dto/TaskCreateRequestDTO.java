package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreateRequestDTO {
    private Long id;

    private String customId;

    private String name;

    private String nameSimple;

    private String note;

    private String text;

    private TaskStatus status;

    private Long estimate;

    private boolean active;
}
