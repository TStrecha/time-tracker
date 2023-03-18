package cz.tstrecha.timetracker.controller.exception;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionException extends LocalizedException {

    public PermissionException(String message, ErrorTypeCode errorTypeCode, String entityType) {
        super(message, errorTypeCode, entityType);
    }

    public PermissionException(String message, ErrorTypeCode errorTypeCode) {
        super(message, errorTypeCode, null);
    }
}
