package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.LocalizedException;
import cz.tstrecha.timetracker.dto.InternalErrorDTO;
import org.springframework.http.ResponseEntity;

public interface ErrorCodeResolver {

    String resolveException(ErrorTypeCode errorTypeCode);

    ResponseEntity<InternalErrorDTO> resolveException(LocalizedException exception);
}
