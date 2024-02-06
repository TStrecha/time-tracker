package cz.tstrecha.timetracker.controller.exception;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import lombok.Data;

@Data
public abstract class LocalizedException extends RuntimeException {

    private final ErrorTypeCode errorTypeCode;
    private final String entityType;

    protected LocalizedException(String message, ErrorTypeCode errorTypeCode) {
        super(message);
        this.errorTypeCode = errorTypeCode;
        this.entityType = null;
    }

    protected LocalizedException(String message, ErrorTypeCode errorTypeCode, String entityType) {
        super(message);
        this.errorTypeCode = errorTypeCode;
        this.entityType = entityType;
    }
}
