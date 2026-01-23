package com.szu.mallsystem.security;

import com.szu.mallsystem.entity.UserToken;
import com.szu.mallsystem.mapper.UserTokenMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final UserTokenMapper userTokenMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            Object typeClaim = claims.get("token_type");
            int typeCode = typeClaim instanceof Number ? ((Number) typeClaim).intValue() : -1;
            TokenType tokenType = TokenType.fromCode(typeCode);
            if (tokenType != TokenType.ACCESS) {
                filterChain.doFilter(request, response);
                return;
            }
            String jti = claims.getId();
            UserToken userToken = userTokenMapper.findByJti(jti);
            if (userToken == null || userToken.getRevoked() != null && userToken.getRevoked() == 1) {
                filterChain.doFilter(request, response);
                return;
            }
            if (userToken.getExpiredAt() != null && userToken.getExpiredAt().isBefore(LocalDateTime.now())) {
                filterChain.doFilter(request, response);
                return;
            }
            String username = claims.getSubject();
            if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException ex) {
            log.debug("JWT expired: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.debug("JWT invalid: {}", ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
