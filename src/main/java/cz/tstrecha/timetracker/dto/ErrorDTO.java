package cz.tstrecha.timetracker.dto;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO {

    private ErrorTypeCode code;
    private String exception;
    private String exceptionMessage;
    private String localizedMessage;
    private String entity;
}
