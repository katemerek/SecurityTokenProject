package com.github.katemerek.securitytokenproject.dto;

import jakarta.validation.constraints.NotBlank;

public record MyUserDtoForGet(@NotBlank String username, String role, Boolean isAccountNonLocked) {
}
