package cz.tstrecha.timetracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!swagger")
public class SecurityConfig {

    @Bean
    @Order(1)
    @Profile("!swagger")
    public SecurityFilterChain filterChainSwagger(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) ->
                requests.requestMatchers(new AntPathRequestMatcher("/openapi/v3/**"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html"))
                        .permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/error"))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
        );
        return http.formLogin()
                .disable()
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .build();
    }

}
