package cz.tstrecha.timetracker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.config.AppConfig;
import cz.tstrecha.timetracker.controller.exception.GenericUnauthorizedException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.LoginResponseDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
import cz.tstrecha.timetracker.service.ContextService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String USER_CONTEXT_CLAIM_KEY = "user";
    private static final String USER_ID_CLAIM_KEY = "userId";
    private static final String AUTHORIZED_AS_USER_CLAIM_KEY = "authorizedAsUserId";


    private final ContextService contextService;

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    private final AppConfig appConfig;

    @Override
    public String generateToken(UserEntity user, ContextUserDTO loggedAs) {
        var contextUserForLoggedAs = loggedAs;

        if(contextUserForLoggedAs == null) {
            contextUserForLoggedAs = user.getUserRelationshipGiving().stream().filter(relation -> relation.getTo() == relation.getFrom())
                    .findFirst()
                    .map(userMapper::userRelationshipEntityToContextUserDTO)
                    .orElseThrow(() -> new IllegalArgumentException(STR."No relationship between the same user found for user [\{user.getId()}]"));
        }

        var claims = Map.of(USER_CONTEXT_CLAIM_KEY, userMapper.toContext(user, contextUserForLoggedAs));
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));
        return Jwts.builder()
                .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + appConfig.getAuth().getAccessTokenDuration().toMillis()))
                .signWith(signInKey, appConfig.getAuth().getSignatureAlgorithm())
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId, Long authorizedAsUserId) {
        var claims = Map.of(USER_ID_CLAIM_KEY, userId, AUTHORIZED_AS_USER_CLAIM_KEY, authorizedAsUserId);
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + appConfig.getAuth().getRefreshTokenDuration().toMillis()))
                .signWith(signInKey, appConfig.getAuth().getSignatureAlgorithm())
                .compact();
    }

    @Override
    public boolean isTokenValid(Claims claims, UserDetails userDetails){
        final var username = getUserEmailFromJwt(claims);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
    }

    @Override
    public boolean isTokenExpired(Claims claims){
        return claims.getExpiration().before(new Date());
    }

    @Override
    public String getUserEmailFromJwt(Claims claims) {
        return claims.getSubject();
    }

    @Override
    public UserContext getUserContext(Claims claims) {
        return objectMapper.convertValue(claims.get(USER_CONTEXT_CLAIM_KEY), UserContext.class);
    }

    @Override
    @Transactional
    public LoginResponseDTO refreshToken(String token) {
        var claims = extractClaims(token).orElseThrow(GenericUnauthorizedException::new);
        if(isTokenExpired(claims)) {
            throw new GenericUnauthorizedException();
        }

        var userId = claims.get(USER_ID_CLAIM_KEY, Long.class);
        var authorizedAsUserId = claims.get(AUTHORIZED_AS_USER_CLAIM_KEY, Long.class);

        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(STR."User entity not found by to id [\{userId}]"));
        var newContext = contextService.getContextFromUser(user, authorizedAsUserId);

        return new LoginResponseDTO(true, generateToken(user, newContext), token);
    }

    @Override
    public Optional<Claims> extractClaims(String authToken){
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));

        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(signInKey)
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();

            return Optional.of(claims);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
