package cz.tstrecha.timetracker.service.annotation;

import cz.tstrecha.timetracker.annotation.InjectLoggedUser;
import cz.tstrecha.timetracker.controller.exception.UserInputException;
import cz.tstrecha.timetracker.dto.LoggedUser;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class LoggedUserParameterResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(InjectLoggedUser.class) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if(parameter.getParameterType() != LoggedUser.class){
            throw new IllegalArgumentException("Parameter annotated InjectLoggedUser should only be of type LoggedUser. " +
                    "Type found [" + parameter.getParameterType() + "]");
        }
        var annotation = parameter.getParameterAnnotation(InjectLoggedUser.class);
        if (annotation == null){
            return null;
        }
        var userContext = ContextUtils.retrieveContextMandatory();
        var userEntity = annotation.fillUserEntity() ? userRepository.findById(userContext.getId()) : null;
                .orElseThrow(() -> new UserInputException("User not found by id [" + userContext.getId() + "]"));
        var loggedUser = userMapper.toLoggedUser(userContext.getLoggedAs(), userEntity);

        return loggedUser;
    }
}
