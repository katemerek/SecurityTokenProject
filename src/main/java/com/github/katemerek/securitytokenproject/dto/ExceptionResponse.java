package com.github.katemerek.securitytokenproject.dto;

import java.time.LocalDateTime;

public record ExceptionResponse(LocalDateTime timestamp, int status, String error, String message) {}