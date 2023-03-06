package cz.tstrecha.timetracker.service;

import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    /**
     *
     * @param user
     * @param loggedAs
     * @return
     */
    String generateToken(UserEntity user, ContextUserDTO loggedAs);

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
    String getUserEmailFromJwt(Claims claims);

    /**
     *
     * @param authToken
     * @return
     */
    UserContext getUserContext(String authToken);

    /**
     *
     * @param authToken
     * @return
     */
    Claims extractClaims(String authToken);
}
