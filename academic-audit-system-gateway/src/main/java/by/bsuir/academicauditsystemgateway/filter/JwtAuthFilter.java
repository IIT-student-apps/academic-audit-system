package by.bsuir.academicauditsystemgateway.filter;

import by.bsuir.academicauditsystemgateway.entity.UserRole;
import by.bsuir.academicauditsystemgateway.service.JwtService;
import by.bsuir.academicauditsystemgateway.service.UserService;
import by.bsuir.academicauditsystemgateway.utils.HttpRequestUtils;
import by.bsuir.academicauditsystemgateway.utils.RequestAttributes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = HttpRequestUtils.extractBearerToken(request);
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String username = jwtService.extractUserName(jwt);
        if (!StringUtils.isEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService
                    .userDetailsService()
                    .loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);

                Long userId = jwtService.extractUserId(jwt);
                request.setAttribute(RequestAttributes.USER_ID, userId);
                UserRole role = jwtService.extractRole(jwt);
                request.setAttribute(RequestAttributes.USER_ROLE, role);
            }
        }
        filterChain.doFilter(request, response);
    }
}
