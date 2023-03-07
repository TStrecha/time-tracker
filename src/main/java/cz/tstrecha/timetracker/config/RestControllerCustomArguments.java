package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.service.annotation.LoggedUserParameterResolver;
import cz.tstrecha.timetracker.service.annotation.UserContextParameterResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableAutoConfiguration
@ComponentScan
public class RestControllerCustomArguments implements WebMvcConfigurer {

    private final UserContextParameterResolver userContextParameterResolver;

    private final LoggedUserParameterResolver loggedUserParameterResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userContextParameterResolver);
        resolvers.add(loggedUserParameterResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}
