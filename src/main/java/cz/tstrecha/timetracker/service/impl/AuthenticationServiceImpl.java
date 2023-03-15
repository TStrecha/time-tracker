package cz.tstrecha.timetracker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tstrecha.timetracker.config.AppConfig;
import cz.tstrecha.timetracker.controller.exception.PermissionException;
import cz.tstrecha.timetracker.dto.ContextUserDTO;
import cz.tstrecha.timetracker.dto.UserContext;
import cz.tstrecha.timetracker.dto.mapper.UserMapper;
import cz.tstrecha.timetracker.repository.UserRelationshipRepository;
import cz.tstrecha.timetracker.repository.UserRepository;
import cz.tstrecha.timetracker.repository.entity.UserEntity;
import cz.tstrecha.timetracker.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String USER_CONTEXT_CLAIM_KEY = "user";
    private static final String USER_ID_CLAIM_KEY = "userId";
    private static final String AUTHORIZED_AS_USER_CLAIM_KEY = "authorizedAsUserId";

    private final UserMapper userMapper;

    private final UserRelationshipRepository userRelationshipRepository;

    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    private final AppConfig appConfig;

    @Override
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
    public UserContext getUserContext(String authToken) {
        return objectMapper.convertValue(extractClaims(authToken).get(USER_CONTEXT_CLAIM_KEY), UserContext.class);
    }

    @Override
    @Transactional
    public String refreshToken(String token) {
        var claims = extractClaims(token);
        
        var userId = claims.get(USER_ID_CLAIM_KEY, Long.class);
        var authorizedAsUserId =  claims.get(AUTHORIZED_AS_USER_CLAIM_KEY, Long.class);

        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User entity not found by to id [" + userId + "]"));

        var userRelationship = user.getUserRelationshipReceiving().stream()
                .filter(relation -> relation.getFrom().getId().equals(authorizedAsUserId))
                .findFirst()
                .orElseThrow(() -> new PermissionException("User doesn't have permission to change context to id [" + authorizedAsUserId + "]"));

        return generateToken(user, userMapper.userRelationshipEntityToContextUserDTO(userRelationship));
    }

    @Override
    public Claims extractClaims(String authToken){
        var signInKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(appConfig.getAuth().getSecretKey()));
        return Jwts.parserBuilder()
                .setSigningKey(signInKey)
                .build()
                .parseClaimsJws(authToken)
                .getBody();
    }
}
