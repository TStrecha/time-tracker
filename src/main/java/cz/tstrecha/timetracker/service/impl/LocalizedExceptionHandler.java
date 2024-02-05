package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.LocalizedException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import cz.tstrecha.timetracker.dto.LoginRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class LocalizedExceptionHandler {

    private final ErrorCodeResolverImpl errorCodeResolver;

    @ExceptionHandler({LocalizedException.class})
    public ResponseEntity<InternalErrorDTO> handleUserInputException(LocalizedException exception){
        return errorCodeResolver.resolveException(exception);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<InternalErrorDTO> handleUserInputException() {
        return errorCodeResolver.resolveException(new UserInputException("Wrong login details.", ErrorTypeCode.BAD_CREDENTIALS, LoginRequestDTO.class.getTypeName()));
    }
}
