package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.constant.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!swagger")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    @Profile("!swagger")
    public SecurityFilterChain filterChainSwagger(HttpSecurity http) throws Exception {
        return http.formLogin()
                .disable()
                .csrf()
                .disable()
                .cors()
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/openapi/v3/**")
                    .permitAll()
                .requestMatchers("/swagger-ui/**")
                    .permitAll()
                .requestMatchers("/swagger-ui.html")
                    .permitAll()
                .requestMatchers("/error")
                    .permitAll()
                .requestMatchers(Constants.V1_CONTROLLER_ROOT + "auth/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
