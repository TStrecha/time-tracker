package cz.tstrecha.timetracker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;

@AllArgsConstructor
public class ResultActionsHandler implements ResultActions {

    private final ResultActions resultActions;

    @Override
    public @NotNull ResultActionsHandler andExpect(ResultMatcher matcher) throws Exception {
        resultActions.andExpect(matcher);
        return this;
    }

    @Override
    public ResultActionsHandler andDo(ResultHandler handler) throws Exception {
        resultActions.andDo(handler);
        return this;
    }

    public ResultActionsHandler and(ResultHandler handler) throws Exception {
        return this.andDo(handler);
    }

    @Override
    public @NotNull MvcResult andReturn() {
        return resultActions.andReturn();
    }

    @SneakyThrows
    public <T> T andReturnAs(Class<T> to) {
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();
        return ObjectMapperUtils.readValue(responseBody, to);
    }

    @SneakyThrows
    public <T> T andReturnAs(TypeReference<T> typeReference) {
        var responseBody = resultActions.andReturn().getResponse().getContentAsString();

        return ObjectMapperUtils.readValue(responseBody, typeReference);
    }
}
