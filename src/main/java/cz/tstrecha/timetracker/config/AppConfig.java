package cz.tstrecha.timetracker.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppConfig {

    private AuthConfig auth;

    @Data
    public static class AuthConfig {

        private Duration tokenDuration;
        private String secretKey;

    }
}
