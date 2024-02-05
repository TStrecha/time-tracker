package cz.tstrecha.timetracker.service.impl;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.LocalizedException;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import cz.tstrecha.timetracker.service.ErrorCodeResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
@Slf4j
public class ErrorCodeResolverImpl implements ErrorCodeResolver {

    public String resolveException(ErrorTypeCode errorTypeCode){
        var locale = LocaleContextHolder.getLocale();
        if (ClassLoader.getSystemResource(String.format("error_codes_%s.properties", locale.getLanguage())) == null){
            locale = new Locale.Builder().setLanguage("cs").setRegion("CZ").build();
        }
        var errorCodeBundle = ResourceBundle.getBundle("error_codes", locale);
        return errorCodeBundle.getString(errorTypeCode.getLocalizationCode());
    }

    public ResponseEntity<InternalErrorDTO> resolveException(LocalizedException exception) {
        var errorDetail = new InternalErrorDTO();

        errorDetail.setException(exception.getClass().getSimpleName());
        errorDetail.setExceptionMessage(exception.getMessage());
        errorDetail.setLocalizedMessage(resolveException(exception.getErrorTypeCode()));
        errorDetail.setEntity(exception.getEntityType());

        var httpStatus = Arrays.stream(exception.getClass().getAnnotations())
                .filter(ResponseStatus.class::isInstance)
                .findFirst()
                .map(annotation -> ((ResponseStatus) annotation).value())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        log.debug("Resolved error detail: {}", errorDetail);

        return new ResponseEntity<>(errorDetail, httpStatus);
    }
}
