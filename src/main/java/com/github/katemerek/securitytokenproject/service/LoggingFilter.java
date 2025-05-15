package com.github.katemerek.securitytokenproject.service;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant startTime = Instant.now();
        String requestInfo = buildRequestInfo(request);
        logger.info("Incoming request: {}", requestInfo);

        try {
            logAuthenticationInfo(request);
            filterChain.doFilter(request, response);
        } finally {
            Duration duration = Duration.between(startTime, Instant.now());
            String responseInfo = buildResponseInfo(request, response, duration);
            logger.info("Request processed: {}", responseInfo);
            logAuthenticationInfo(request);
        }
    }


    private String buildRequestInfo(HttpServletRequest request) {
        return String.format(
                "Method=%s URI=%s Headers=%s",
                request.getMethod(),
                request.getRequestURI(),
                getRequestHeaders(request)
        );
    }

    private String buildResponseInfo(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Duration duration) {
        return String.format(
                "Method=%s URI=%s Status=%d Time=%dms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration.toMillis()
        );
    }

    private void logAuthenticationInfo(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String username = SecurityContextHolder.getContext().getAuthentication() != null
                    ? SecurityContextHolder.getContext().getAuthentication().getName()
                    : "anonymous";

            String authInfo = String.format(
                    "User=%s JWT=%s",
                    username,
                    authHeader != null && authHeader.startsWith("Bearer ") ? "present" : "none"
            );

            logger.debug("Authentication info: {}", authInfo);

        } catch (Exception e) {
            logger.warn("Failed to log authentication info: {}", e.getMessage());
        }
    }

    private String getRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder("{");
        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> {
                    String headerValue = request.getHeader(headerName);
                    // Маскируем токены
                    if ("Authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                        headerValue = "Bearer *masked*";
                    }
                    headers.append(headerName)
                            .append("=")
                            .append(headerValue)
                            .append(", ");
                });
        if (headers.length() > 1) {
            headers.delete(headers.length() - 2, headers.length());
        }
        headers.append("}");
        return headers.toString();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Исключаем из логирования статические ресурсы и актуаторы
        String path = request.getRequestURI();
        return path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/actuator/")
                || path.startsWith("/favicon.ico");
    }
}
