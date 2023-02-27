package cz.tstrecha.timetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.config.AppConfig;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String USER_CONTEXT_CLAIM_KEY = "user";

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    private final AppConfig appConfig;

    public String generateToken(UserEntity user, ContextUserDTO loggedAs) {
        var contextUserForLoggedAs = loggedAs == null ?
                user.getUserRelationshipGiving().stream().filter(relation -> relation.getTo() == relation.getFrom())
                        .findFirst()
                        .map(userMapper::userRelationshipEntityToContextUserDTO)
                        .orElseThrow(() -> new IllegalArgumentException("No relationship between the same user found for user [" + user.getId() + "]"))
                : loggedAs;

        var claims = Map.of(USER_CONTEXT_CLAIM_KEY, userMapper.toContext(user, contextUserForLoggedAs));
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + appConfig.getAuth().getTokenDuration().toMillis()))
                .signWith(signInKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(Claims claims, UserDetails userDetails){
        final var username = getUserEmailFromJwt(claims);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
    }

    public boolean isTokenExpired(Claims claims){
        return claims.getExpiration().before(new Date());
    }

    public String getUserEmailFromJwt(Claims claims) {
        return claims.getSubject();
    }

    public UserContext getUserContext(String authToken) {
        return objectMapper.convertValue(extractClaims(authToken).get(USER_CONTEXT_CLAIM_KEY), UserContext.class);
    }

    public Claims extractClaims(String authToken){
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));
        return Jwts.parserBuilder()
                .setSigningKey(signInKey)
                .build()
                .parseClaimsJws(authToken)
                .getBody();
    }
}
