package com.github.katemerek.securitytokenproject.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.katemerek.securitytokenproject.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;
    private final MyUserDetailService userDetailsService;

    public JwtAuthenticationFilter(JWTUtils jwtUtils, UserDetailsService userDetailsService, MyUserDetailService userDetailsService1) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService1;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Шаг 1: Извлечение заголовка авторизации из запроса
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userNameFromToken;

        // Шаг 2: Проверка наличия заголовка авторизации
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Шаг 3: Извлечение токена из заголовка
        jwtToken = authHeader.substring(7);

        // Шаг 4: Извлечение имени пользователя из JWT токена
        userNameFromToken = jwtUtils.extractUsername(jwtToken);

        // Шаг 5: Проверка аутентификации и валидация токена
        if (userNameFromToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            MyUserDetails userDetails = userDetailsService.loadUserByUsername(userNameFromToken);
            if (jwtUtils.isTokenValid(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken token =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Шаг 6: Создание нового контекста безопасности
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        }

        // Шаг 7: Передача запроса на дальнейшую обработку в фильтрующий цепочке
        filterChain.doFilter(request, response);
    }
}