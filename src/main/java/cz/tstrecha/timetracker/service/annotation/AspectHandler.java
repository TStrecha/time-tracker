package cz.tstrecha.timetracker.service.annotation;

import cz.tstrecha.timetracker.annotation.PermissionCheck;
import cz.tstrecha.timetracker.annotation.PermissionChecks;
import cz.tstrecha.timetracker.annotation.SecuredValue;
import cz.tstrecha.timetracker.constant.ErrorTypeCode;
import cz.tstrecha.timetracker.constant.PermissionCheckOperation;
import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.util.ContextUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class AspectHandler {

    @Before("@annotation(checks)")
    public void handlePermissionChecks(JoinPoint jp, PermissionChecks checks) {
        var context = ContextUtils.retrieveContextMandatory();
        if (checks.operation() == PermissionCheckOperation.AND) {
            if (!Arrays.stream(checks.value()).allMatch(permission -> ContextUtils.hasPermissions(context, permission.value()))) {
                throw new PermissionException("User context doesn't have all required permissions.", ErrorTypeCode.USER_DOES_NOT_HAVE_ALL_PERMISSIONS);
            }
        } else { // PermissionCheckOperation.OR
            if (Arrays.stream(checks.value()).noneMatch(permission -> ContextUtils.hasPermissions(context, permission.value()))) {
                throw new PermissionException("User context doesn't have one of the required permissions.", ErrorTypeCode.USER_DOES_NOT_HAVE_ONE_PERMISSION);
            }
        }
    }

    @Before("@annotation(check)")
    public void handlePermissionChecks(JoinPoint jp, PermissionCheck check) {
        var context = ContextUtils.retrieveContextMandatory();
        if (!ContextUtils.hasPermissions(context, check.value())) {
            throw new PermissionException("User context doesn't have the required permission.", ErrorTypeCode.USER_DOES_NOT_HAVE_REQUIRED_PERMISSIONS);
        }
    }

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