package cz.tstrecha.timetracker.config;

import cz.tstrecha.timetracker.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHORIZATION_HEADER_BEARER_PREFIX = "Bearer ";

    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final var authHeader = request.getHeader(AUTHORIZATION_HEADER_NAME);
        if (authHeader == null || !authHeader.startsWith(AUTHORIZATION_HEADER_BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        final var authToken = authHeader.substring(AUTHORIZATION_HEADER_BEARER_PREFIX.length());

        authenticationService.extractClaims(authToken).ifPresent(claims -> {
            final var userEmail = authenticationService.getUserEmailFromJwt(claims);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(userEmail);
                var userContext = authenticationService.getUserContext(claims);

                if (authenticationService.isTokenValid(claims, userDetails)) {
                    var usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(authenticationService.getUserContext(claims), null, userContext.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        });

        filterChain.doFilter(request, response);
    }
}
