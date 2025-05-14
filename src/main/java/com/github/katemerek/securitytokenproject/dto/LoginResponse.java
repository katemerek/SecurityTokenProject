package com.github.katemerek.securitytokenproject.dto;

import org.springframework.http.HttpStatusCode;

public record LoginResponse(HttpStatusCode statusCode, String token, String refreshToken, String Role,
                            String ExpirationTime, String message) {}
