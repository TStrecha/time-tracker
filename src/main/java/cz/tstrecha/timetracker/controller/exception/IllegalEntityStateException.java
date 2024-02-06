package cz.tstrecha.timetracker.controller.exception;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class IllegalEntityStateException extends UserInputException {

    public IllegalEntityStateException(String message, ErrorTypeCode errorTypeCode, String entityType) {
        super(message, errorTypeCode, entityType);
    }


    public IllegalEntityStateException(String message, ErrorTypeCode errorTypeCode, Class<?> target) {
        super(message, errorTypeCode, target);
    }

    public IllegalEntityStateException(String message, ErrorTypeCode errorTypeCode) {
        super(message, errorTypeCode);
    }
}
