package cz.tstrecha.timetracker.utils.assertions;

import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.IllegalEntityStateException;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ErrorDTO;
import cz.tstrecha.timetracker.utils.ObjectMapperUtils;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

@AllArgsConstructor
public class UserInputExceptionHandler implements ResultHandler {

    public static UserInputExceptionHandler handleUserInputException(ErrorTypeCode errorTypeCode) {
        return new UserInputExceptionHandler(errorTypeCode, UserInputException.class);
    }

    public static UserInputExceptionHandler handleIllegalEntityStateException(ErrorTypeCode errorTypeCode) {
        return new UserInputExceptionHandler(errorTypeCode, IllegalEntityStateException.class);
    }

    private final ErrorTypeCode code;
    private final Class<? extends UserInputException> expectedExceptionClass;

    @Override
    public void handle(MvcResult result) throws Exception {
        var content = result.getResponse().getContentAsString();
        var exceptionDTO = ObjectMapperUtils.readValue(content, ErrorDTO.class);

        Assertions.assertEquals(this.expectedExceptionClass.getSimpleName(), exceptionDTO.getException());
        Assertions.assertEquals(this.code, exceptionDTO.getCode());

        Assertions.assertNotNull(exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getExceptionMessage());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }
}
