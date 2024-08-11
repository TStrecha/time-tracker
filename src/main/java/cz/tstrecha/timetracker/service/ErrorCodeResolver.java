package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.LocalizedException;
import cz.tstrecha.timetracker.dto.ErrorDTO;
import org.springframework.http.ResponseEntity;

public interface ErrorCodeResolver {

    String resolveException(ErrorTypeCode errorTypeCode);

    ResponseEntity<ErrorDTO> resolveException(LocalizedException exception);
}
