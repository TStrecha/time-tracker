package cz.tstrecha.timetracker.controller.exception;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends LocalizedException {

    public UnauthorizedException(String message, ErrorTypeCode errorTypeCode, String entityType) {
        super(message, errorTypeCode, entityType);
    }

    public UnauthorizedException(String message, ErrorTypeCode errorTypeCode, Class<?> target) {
        super(message, errorTypeCode, target.getSimpleName());
    }

    public UnauthorizedException(String message, ErrorTypeCode errorTypeCode) {
        super(message, errorTypeCode, null);
    }
}
