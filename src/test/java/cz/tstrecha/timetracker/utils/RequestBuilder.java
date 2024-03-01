package cz.tstrecha.timetracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.tstrecha.timetracker.config.JwtAuthenticationFilter;
import cz.tstrecha.timetracker.constant.Constants;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static cz.tstrecha.timetracker.config.JwtAuthenticationFilter.AUTHORIZATION_HEADER_NAME;

@Data
public class RequestBuilder {

    public static RequestBuilder buildRequest(HttpMethod method, String urlTemplate) {
        return new RequestBuilder(MockMvcRequestBuilders.request(method, Constants.V1_CONTROLLER_ROOT + urlTemplate));
    }

    private final MockHttpServletRequestBuilder httpRequestBuilder;
    private final ObjectMapper objectMapper;

    public RequestBuilder(MockHttpServletRequestBuilder httpRequestBuilder) {
        this.httpRequestBuilder = httpRequestBuilder;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public RequestBuilder withAuthorization(String jwtToken) {
        httpRequestBuilder.header(AUTHORIZATION_HEADER_NAME, JwtAuthenticationFilter.AUTHORIZATION_HEADER_BEARER_PREFIX + jwtToken);
        return this;
    }

    public RequestBuilder withAuthorization(IntegrationTest.TokenHolder tokenHolder) {
        return withAuthorization(tokenHolder.getToken());
    }

    @SneakyThrows
    public RequestBuilder withBody(Object body) {
        httpRequestBuilder.content(objectMapper.writeValueAsString(body));
        httpRequestBuilder.contentType(MediaType.APPLICATION_JSON);
        return this;
    }

    @SneakyThrows
    public RequestBuilder withBody(String body) {
        httpRequestBuilder.content(body);
        return this;
    }

    @SneakyThrows
    public ResultActions performWith(MockMvc mvc) {
        return mvc.perform(httpRequestBuilder);
    }
}
