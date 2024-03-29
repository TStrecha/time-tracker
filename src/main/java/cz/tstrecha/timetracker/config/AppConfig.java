package cz.tstrecha.timetracker.config;

import io.jsonwebtoken.SignatureAlgorithm;
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

    private String defaultTimeZone;

    private boolean createDefaultUsers;

    @Data
    public static class AuthConfig {

        private Duration accessTokenDuration;
        private Duration refreshTokenDuration;
        private String secretKey;
        private SignatureAlgorithm signatureAlgorithm;
    }
}
