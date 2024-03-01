package cz.tstrecha.timetracker.utils.assertions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

@AllArgsConstructor
public class UserInputExceptionHandler implements ResultHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static UserInputExceptionHandler handleUserInputException(ErrorTypeCode errorTypeCode) {
        return new UserInputExceptionHandler(errorTypeCode);
    }

    private final ErrorTypeCode code;

    @Override
    public void handle(MvcResult result) throws Exception {
        var content = result.getResponse().getContentAsString();
        var exceptionDTO = objectMapper.readValue(content, ErrorDTO.class);

        Assertions.assertEquals(UserInputException.class.getSimpleName(), exceptionDTO.getException());
        Assertions.assertEquals(this.code, exceptionDTO.getCode());

        Assertions.assertNotNull(exceptionDTO.getEntity());
        Assertions.assertNotNull(exceptionDTO.getExceptionMessage());
        Assertions.assertNotNull(exceptionDTO.getLocalizedMessage());
    }
}
