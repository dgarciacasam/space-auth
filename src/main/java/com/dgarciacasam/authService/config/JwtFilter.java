package com.dgarciacasam.authService.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dgarciacasam.authService.services.CustomUserDetailsService;
import com.dgarciacasam.authService.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod();
        if (logger.isTraceEnabled()) {
            logger.trace("[JWT] Inicio filtro -> {} {}", httpMethod, requestUri);
        }

        String jwt = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Buscar token en el header Authorization
        if (jwt != null && jwt.startsWith("Bearer ")) {
            token = jwt.substring(7);
            try {
                username = jwtService.extractUserName(token);
                logger.debug("[JWT] Token en header válido sintácticamente. user={} uri={}", username, requestUri);
            } catch (Exception ex) {
                logger.warn("[JWT] Fallo extrayendo usuario del token del header. uri={} causa={}", requestUri,
                        ex.getMessage());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[JWT] Header Authorization ausente o sin Bearer. uri={}", requestUri);
            }
        }

        // Si no se encontró en el header, buscar en las cookies
        if (token == null) {
            var cookies = request.getCookies();
            if (cookies != null) {
                for (var cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        try {
                            username = jwtService.extractUserName(token);
                            logger.debug("[JWT] Token encontrado en cookie. user={} uri={}", username, requestUri);
                        } catch (Exception ex) {
                            logger.warn("[JWT] Fallo extrayendo usuario del token de cookie. uri={} causa={}",
                                    requestUri, ex.getMessage());
                        }
                        break;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[JWT] No hay cookies en la petición. uri={}", requestUri);
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = context.getBean(CustomUserDetailsService.class).loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("[JWT] Autenticación establecida para user={} uri={}", username, requestUri);
                } else {
                    logger.warn("[JWT] Token inválido para user={} uri={}", username, requestUri);
                }
            } catch (Exception ex) {
                logger.error("[JWT] Error durante la validación/autenticación. user={} uri={} causa={}", username,
                        requestUri, ex.getMessage());
            }
        } else if (username == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("[JWT] No se pudo determinar usuario desde el token. uri={}", requestUri);
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("[JWT] SecurityContext ya contiene autenticación. uri={}", requestUri);
            }
        }
        filterChain.doFilter(request, response);
        if (logger.isTraceEnabled()) {
            logger.trace("[JWT] Fin filtro -> {} {}", httpMethod, requestUri);
        }
    }

}
