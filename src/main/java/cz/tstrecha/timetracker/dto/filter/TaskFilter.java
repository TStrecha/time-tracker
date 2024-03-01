package cz.tstrecha.timetracker.dto.filter;

import cz.tstrecha.timetracker.constant.SortDirection;
import cz.tstrecha.timetracker.constant.TaskFilterField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilter {

    private Map<TaskFilterField, String> fieldFilters;
    private TaskFilterField sort = TaskFilterField.ID;
    private SortDirection sortDirection;

    private Integer rows = 15;
    private Integer pageNumber = 0;
}
