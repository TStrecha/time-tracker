package cz.tstrecha.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextUserDTO {

    private Long id;
    private String email;
    private String fullName;
    private boolean secureValues;
}
