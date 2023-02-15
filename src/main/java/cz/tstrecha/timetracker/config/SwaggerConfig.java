package cz.tstrecha.timetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String AUTHENTICATION_TOKEN_HEADER = "Auth-Token";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().components(new Components().addSecuritySchemes(AUTHENTICATION_TOKEN_HEADER, new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                                                                                                        .name(AUTHENTICATION_TOKEN_HEADER)
                                                                                                                        .scheme("http")
                                                                                                                        .in(SecurityScheme.In.HEADER)));
    }
}
