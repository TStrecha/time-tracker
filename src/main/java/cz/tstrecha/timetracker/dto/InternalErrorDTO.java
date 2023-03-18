package cz.tstrecha.timetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalErrorDTO {

    private String exception;

    private String exceptionMessage;

    private String localizedMessage;

    private String entity;
}
