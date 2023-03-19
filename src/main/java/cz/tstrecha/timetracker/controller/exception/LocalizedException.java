package cz.tstrecha.timetracker.controller.exception;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import lombok.Data;

@Data
public abstract class LocalizedException extends RuntimeException {

    private ErrorTypeCode errorTypeCode;
    private String entityType;

    public LocalizedException(String message, ErrorTypeCode errorTypeCode) {
        super(message);
        this.errorTypeCode = errorTypeCode;
    }

    public LocalizedException(String message, ErrorTypeCode errorTypeCode, String entityType) {
        super(message);
        this.errorTypeCode = errorTypeCode;
        this.entityType = entityType;
    }
}
