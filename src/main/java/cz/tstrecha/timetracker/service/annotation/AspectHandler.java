package cz.tstrecha.timetracker.service.annotation;

import cz.tstrecha.timetracker.annotation.SecuredValue;
import cz.tstrecha.timetracker.util.ContextUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
// This feature is completely work in progress. There is no task explaining this feature and will still require some work
public class AspectHandler {

    private static final Map<Class<? extends Number>, Object> SECURE_TYPE_VALUE_MAPPING = Map.of(
            Float.class, 1.0f,
            BigDecimal.class, BigDecimal.ONE
    );

    @AfterReturning(value = "@annotation(cz.tstrecha.timetracker.annotation.SecureValues)", returning = "result")
    public void handleSecureValues(Object result) {
        var context = ContextUtils.retrieveContextMandatory();
        if (context.getLoggedAs().isSecureValues()) {
            Arrays.stream(result.getClass().getDeclaredFields())
                    .filter(field -> field.getAnnotation(SecuredValue.class) != null)
                    .forEach(field -> {
                        var mapping = SECURE_TYPE_VALUE_MAPPING.getOrDefault(field.getType(), null);
                        if (mapping != null) {
                            ReflectionUtils.setField(field, result, mapping);
                        }
                    });
        }
    }
}