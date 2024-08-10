package cz.tstrecha.timetracker.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectMapperUtils {

    public final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @SneakyThrows
    public <T> T readValue(String content, Class<T> valueType) {
        return objectMapper.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(String content, TypeReference<T> typeReference) {
        return objectMapper.readValue(content, typeReference);
    }

    @SneakyThrows
    public String writeValueAsString(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    public String writeValueAsPrettyString(Object value) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }
}
