package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface AuthenticationService {

    /**
     *
     * @param user
     * @param loggedAs
     * @return
     */
    String generateToken(UserEntity user, @Nullable ContextUserDTO loggedAs);

    /**
     *
     * @param userId
     * @param authorizedAsUserId
     * @return
     */
    String generateRefreshToken(Long userId, Long authorizedAsUserId);

    /**
     *
     * @param claims
     * @param userDetails
     * @return
     */
    boolean isTokenValid(Claims claims, UserDetails userDetails);

    /**
     *
     * @param claims
     * @return
     */
    boolean isTokenExpired(Claims claims);

    /**
     *
     * @param claims
     * @return
     */
    String getUserEmailFromJwt(Claims claims);

    /**
     *
     * @param claims
     * @return
     */
    UserContext getUserContext(Claims claims);

    /**
     *
     * @param token
     * @return
     */
    LoginResponseDTO refreshToken(String token);

    /**
     *
     * @param authToken
     * @return
     */
    Optional<Claims> extractClaims(String authToken);
}
