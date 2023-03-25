package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.SortDirection;
import cz.tstrecha.timetracker.constant.TaskField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilter {
    private Map<TaskField, String> fieldFilters;

    private TaskField sort = TaskField.ID;

    private SortDirection sortDirection;

    private Integer rows = 15;

    private Integer pageNumber = 0;
}
