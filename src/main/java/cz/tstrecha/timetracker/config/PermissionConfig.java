package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.service.PermissionService;
import cz.tstrecha.timetracker.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Configuration
@EnableMethodSecurity(prePostEnabled = false)
@RequiredArgsConstructor
public class PermissionConfig implements PermissionEvaluator {

    private final PermissionService permissionService;

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor preAuthorizeAuthorizationMethodInterceptor(PermissionConfig permissionConfig) {
        var expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionConfig);

        var authorizationManager = new PreAuthorizeAuthorizationManager();
        authorizationManager.setExpressionHandler(expressionHandler);

        return AuthorizationManagerBeforeMethodInterceptor.preAuthorize(authorizationManager);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return permissionService.hasPermission(ContextUtils.retrieveContextMandatory(), targetDomainObject, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        return permissionService.hasPermission(ContextUtils.retrieveContextMandatory(), targetType, targetId, permission.toString());
    }
}