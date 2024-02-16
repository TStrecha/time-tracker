package cz.tstrecha.timetracker.service.annotation;

import cz.tstrecha.timetracker.annotation.InjectUserContext;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.util.ContextUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserContextParameterResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(InjectUserContext.class) != null;
    }

    @Override
    public UserContext resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        if(parameter.getParameterType() != UserContext.class){
            throw new IllegalArgumentException(STR."Parameter annotated InjectUserContext should only be of type UserContext. Type found [\{parameter.getParameterType()}]");
        }
        var annotation = parameter.getParameterAnnotation(InjectUserContext.class);
        if(annotation == null){
            return null;
        }
        if(annotation.required()) {
            return ContextUtils.retrieveContextMandatory();
        }
        return ContextUtils.retrieveContext().orElse(null);
    }
}
