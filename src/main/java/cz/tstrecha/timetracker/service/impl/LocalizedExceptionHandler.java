package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.config.ErrorCodeResolver;
import cz.tstrecha.timetracker.controller.exception.LocalizedException;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;

@ControllerAdvice
@RequiredArgsConstructor
public class LocalizedExceptionHandler {

    private final ErrorCodeResolver errorCodeResolver;

    @ExceptionHandler({LocalizedException.class})
    public ResponseEntity<InternalErrorDTO> handleUserInputException(LocalizedException exception){

        var output = new InternalErrorDTO();

        output.setException(exception.getClass().getSimpleName());
        output.setExceptionMessage(exception.getMessage());
        output.setLocalizedMessage(errorCodeResolver.resolveException(exception.getErrorTypeCode()));
        output.setEntity(exception.getEntityType());

        var httpStatus = Arrays.stream(exception.getClass().getAnnotations())
                .filter(annotation -> annotation instanceof ResponseStatus)
                .findFirst().map(annotation -> ((ResponseStatus) annotation).value()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(output, httpStatus);
    }
}
